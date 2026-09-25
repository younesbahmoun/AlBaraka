package services;

import exception.RepositoryException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class RapportService {
    private final String url;
    private final String user;
    private final String password;

    public RapportService(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection ouvrirConnexion() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public record TopClient(int id, String nom, BigDecimal soldeTotal) {}

    public record RapportMensuel(
            String type,
            long nombreTransactions,
            BigDecimal volumeTotal
    ) {}

    public record CompteInactif(
            long id,
            String numero,
            LocalDateTime derniereTransaction
    ) {}

    public record TransactionSuspecte(
            int id,
            int idClient,
            BigDecimal montant,
            String lieu,
            long operationsDansLaMinute
    ) {}

    public List<TopClient> top5Clients() {
        String sql = """
                SELECT c.id, c.nom,
                       COALESCE(SUM(co.solde), 0) AS solde_total
                FROM clients c
                LEFT JOIN comptes co ON co.id_client = c.id
                GROUP BY c.id, c.nom
                ORDER BY solde_total DESC
                LIMIT 5
                """;

        List<TopClient> clients = new ArrayList<>();

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                clients.add(new TopClient(
                        result.getInt("id"),
                        result.getString("nom"),
                        result.getBigDecimal("solde_total")
                ));
            }

            return clients;

        } catch (SQLException e) {
            throw new RepositoryException("Erreur dans le top 5 des clients", e);
        }
    }

    public List<RapportMensuel> rapportMensuel(int annee, int mois) {
        YearMonth periode = YearMonth.of(annee, mois);

        String sql = """
                SELECT type_transaction,
                       COUNT(*) AS nombre,
                       SUM(montant) AS volume_total
                FROM transactions
                WHERE date_transaction >= ?
                  AND date_transaction < ?
                GROUP BY type_transaction
                ORDER BY type_transaction
                """;

        List<RapportMensuel> rapport = new ArrayList<>();

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(
                    1,
                    Timestamp.valueOf(periode.atDay(1).atStartOfDay())
            );
            statement.setTimestamp(
                    2,
                    Timestamp.valueOf(
                            periode.plusMonths(1).atDay(1).atStartOfDay()
                    )
            );

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    rapport.add(new RapportMensuel(
                            result.getString("type_transaction"),
                            result.getLong("nombre"),
                            result.getBigDecimal("volume_total")
                    ));
                }
            }

            return rapport;

        } catch (SQLException e) {
            throw new RepositoryException("Erreur dans le rapport mensuel", e);
        }
    }

    public List<CompteInactif> comptesInactifs(int nombreMois) {
        if (nombreMois <= 0) {
            throw new IllegalArgumentException(
                    "Le nombre de mois doit être supérieur à zéro."
            );
        }

        String sql = """
                SELECT c.id, c.numero,
                       MAX(t.date_transaction) AS derniere_transaction
                FROM comptes c
                LEFT JOIN transactions t
                    ON t.id_compte = c.id
                    OR t.id_compte_destination = c.id
                GROUP BY c.id, c.numero
                HAVING MAX(t.date_transaction) < ?
                ORDER BY derniere_transaction
                """;

        List<CompteInactif> comptes = new ArrayList<>();

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setTimestamp(
                    1,
                    Timestamp.valueOf(
                            LocalDateTime.now().minusMonths(nombreMois)
                    )
            );

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    comptes.add(new CompteInactif(
                            result.getLong("id"),
                            result.getString("numero"),
                            result.getTimestamp("derniere_transaction")
                                    .toLocalDateTime()
                    ));
                }
            }

            return comptes;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Erreur dans la recherche des comptes inactifs", e
            );
        }
    }

    public List<TransactionSuspecte> transactionsSuspectes(
            BigDecimal seuil,
            String lieuHabituel
    ) {
        if (seuil == null || seuil.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Le seuil doit être supérieur à zéro."
            );
        }

        if (lieuHabituel == null || lieuHabituel.isBlank()) {
            throw new IllegalArgumentException(
                    "Le lieu habituel est obligatoire."
            );
        }

        String sql = """
                WITH analyse AS (
                    SELECT t.id, c.id_client, t.montant, t.lieu,
                           (
                               SELECT COUNT(*)
                               FROM transactions t2
                               JOIN comptes c2 ON c2.id = t2.id_compte
                               WHERE c2.id_client = c.id_client
                                 AND t2.date_transaction
                                     > t.date_transaction - INTERVAL '1 minute'
                                 AND (
                                     t2.date_transaction < t.date_transaction
                                     OR (
                                         t2.date_transaction = t.date_transaction
                                         AND t2.id <= t.id
                                     )
                                 )
                           ) AS operations_dans_la_minute
                    FROM transactions t
                    JOIN comptes c ON c.id = t.id_compte
                )
                SELECT id, id_client, montant, lieu,
                       operations_dans_la_minute
                FROM analyse
                WHERE montant > ?
                   OR LOWER(lieu) <> LOWER(?)
                   OR operations_dans_la_minute >= 3
                ORDER BY id
                """;

        List<TransactionSuspecte> transactions = new ArrayList<>();

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, seuil);
            statement.setString(2, lieuHabituel.trim());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    transactions.add(new TransactionSuspecte(
                            result.getInt("id"),
                            result.getInt("id_client"),
                            result.getBigDecimal("montant"),
                            result.getString("lieu"),
                            result.getLong("operations_dans_la_minute")
                    ));
                }
            }

            return transactions;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Erreur dans la détection des transactions suspectes", e
            );
        }
    }
}