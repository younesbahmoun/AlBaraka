package services;

import enums.TypeTransaction;
import exception.RepositoryException;
import exception.TransactionException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    private final String url;
    private final String user;
    private final String password;

    public TransactionService(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public record LigneTransaction(
            int id,
            LocalDateTime date,
            BigDecimal montant,
            TypeTransaction type,
            String lieu,
            long idCompte,
            Long idCompteDestination
    ) {}

    public record Groupe(
            String valeur,
            long nombre,
            BigDecimal total
    ) {}

    public record Statistiques(
            long nombre,
            BigDecimal total,
            BigDecimal moyenne
    ) {}

    private Connection ouvrirConnexion() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private LigneTransaction lireTransaction(ResultSet result)
            throws SQLException {
        long destination = result.getLong("id_compte_destination");
        Long idDestination = result.wasNull() ? null : destination;

        return new LigneTransaction(
                result.getInt("id"),
                result.getTimestamp("date_transaction").toLocalDateTime(),
                result.getBigDecimal("montant"),
                TypeTransaction.valueOf(result.getString("type_transaction")),
                result.getString("lieu"),
                result.getLong("id_compte"),
                idDestination
        );
    }

    private List<LigneTransaction> lireListe(PreparedStatement statement)
            throws SQLException {
        List<LigneTransaction> transactions = new ArrayList<>();

        try (ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                transactions.add(lireTransaction(result));
            }
        }

        return transactions;
    }

    // Un virement reçu apparaît aussi dans l'historique du compte destinataire.
    public List<LigneTransaction> listerParCompte(long idCompte) {
        String sql = """
                SELECT *
                FROM transactions
                WHERE id_compte = ? OR id_compte_destination = ?
                ORDER BY date_transaction DESC, id DESC
                """;

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, idCompte);
            statement.setLong(2, idCompte);
            return lireListe(statement);

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Erreur lors de la lecture des transactions du compte", e
            );
        }
    }

    public List<LigneTransaction> listerParClient(int idClient) {
        String sql = """
                SELECT DISTINCT t.*
                FROM transactions t
                JOIN comptes c ON c.id = t.id_compte
                LEFT JOIN comptes d ON d.id = t.id_compte_destination
                WHERE c.id_client = ? OR d.id_client = ?
                ORDER BY t.date_transaction DESC, t.id DESC
                """;

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idClient);
            statement.setInt(2, idClient);
            return lireListe(statement);

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Erreur lors de la lecture des transactions du client", e
            );
        }
    }

    // Les paramètres null ne sont pas utilisés comme filtres.
    public List<LigneTransaction> filtrer(
            int idClient,
            BigDecimal montantMin,
            BigDecimal montantMax,
            TypeTransaction type,
            LocalDate date,
            String lieu
    ) {
        if (montantMin != null && montantMin.signum() < 0) {
            throw new TransactionException("Le montant minimum est invalide.");
        }
        if (montantMax != null && montantMax.signum() < 0) {
            throw new TransactionException("Le montant maximum est invalide.");
        }
        if (montantMin != null && montantMax != null
                && montantMin.compareTo(montantMax) > 0) {
            throw new TransactionException(
                    "Le minimum dépasse le maximum."
            );
        }

        String sql = """
                SELECT DISTINCT t.*
                FROM transactions t
                JOIN comptes c ON c.id = t.id_compte
                LEFT JOIN comptes d ON d.id = t.id_compte_destination
                WHERE (c.id_client = ? OR d.id_client = ?)
                  AND (CAST(? AS NUMERIC) IS NULL OR t.montant >= ?)
                  AND (CAST(? AS NUMERIC) IS NULL OR t.montant <= ?)
                  AND (CAST(? AS VARCHAR) IS NULL
                       OR t.type_transaction = ?)
                  AND (CAST(? AS DATE) IS NULL
                       OR t.date_transaction::date = ?)
                  AND (CAST(? AS VARCHAR) IS NULL
                       OR LOWER(t.lieu) LIKE LOWER(?))
                ORDER BY t.date_transaction DESC, t.id DESC
                """;

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idClient);
            statement.setInt(2, idClient);
            statement.setBigDecimal(3, montantMin);
            statement.setBigDecimal(4, montantMin);
            statement.setBigDecimal(5, montantMax);
            statement.setBigDecimal(6, montantMax);

            String typeTexte = type == null ? null : type.name();
            statement.setString(7, typeTexte);
            statement.setString(8, typeTexte);

            statement.setObject(9, date);
            statement.setObject(10, date);

            String lieuRecherche = lieu == null || lieu.isBlank()
                    ? null : "%" + lieu.trim() + "%";
            statement.setString(11, lieuRecherche);
            statement.setString(12, lieuRecherche);

            return lireListe(statement);

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Erreur lors du filtrage des transactions", e
            );
        }
    }

    public List<Groupe> regrouperParType(int idClient) {
        String sql = """
                SELECT t.type_transaction AS valeur,
                       COUNT(*) AS nombre,
                       SUM(t.montant) AS total
                FROM transactions t
                JOIN comptes c ON c.id = t.id_compte
                WHERE c.id_client = ?
                GROUP BY t.type_transaction
                ORDER BY t.type_transaction
                """;

        return lireGroupes(sql, idClient);
    }

    public List<Groupe> regrouperParMois(int idClient) {
        String sql = """
                SELECT TO_CHAR(t.date_transaction, 'YYYY-MM') AS valeur,
                       COUNT(*) AS nombre,
                       SUM(t.montant) AS total
                FROM transactions t
                JOIN comptes c ON c.id = t.id_compte
                WHERE c.id_client = ?
                GROUP BY TO_CHAR(t.date_transaction, 'YYYY-MM')
                ORDER BY valeur
                """;

        return lireGroupes(sql, idClient);
    }

    private List<Groupe> lireGroupes(String sql, int idClient) {
        List<Groupe> groupes = new ArrayList<>();

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idClient);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    groupes.add(new Groupe(
                            result.getString("valeur"),
                            result.getLong("nombre"),
                            result.getBigDecimal("total")
                    ));
                }
            }

            return groupes;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Erreur lors du regroupement des transactions", e
            );
        }
    }

    public Statistiques statistiquesCompte(long idCompte) {
        String sql = """
                SELECT COUNT(*) AS nombre,
                       COALESCE(SUM(montant), 0) AS total,
                       COALESCE(ROUND(AVG(montant), 2), 0) AS moyenne
                FROM transactions
                WHERE id_compte = ? OR id_compte_destination = ?
                """;

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, idCompte);
            statement.setLong(2, idCompte);
            return lireStatistiques(statement);

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Erreur lors du calcul des statistiques du compte", e
            );
        }
    }

    public Statistiques statistiquesClient(int idClient) {
        String sql = """
                SELECT COUNT(*) AS nombre,
                       COALESCE(SUM(t.montant), 0) AS total,
                       COALESCE(ROUND(AVG(t.montant), 2), 0) AS moyenne
                FROM transactions t
                JOIN comptes c ON c.id = t.id_compte
                WHERE c.id_client = ?
                """;

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idClient);
            return lireStatistiques(statement);

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Erreur lors du calcul des statistiques du client", e
            );
        }
    }

    private Statistiques lireStatistiques(PreparedStatement statement)
            throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            result.next();
            return new Statistiques(
                    result.getLong("nombre"),
                    result.getBigDecimal("total"),
                    result.getBigDecimal("moyenne")
            );
        }
    }

    // Suspecte si montant > seuil, lieu différent, ou au moins
    // 3 opérations du client en une minute (transaction courante incluse).
    public List<LigneTransaction> detecterSuspectes(
            BigDecimal seuil,
            String lieuHabituel
    ) {
        if (seuil == null || seuil.signum() <= 0) {
            throw new TransactionException(
                    "Le seuil doit être supérieur à zéro."
            );
        }
        if (lieuHabituel == null || lieuHabituel.isBlank()) {
            throw new TransactionException(
                    "Le lieu habituel est obligatoire."
            );
        }

        String sql = """
                SELECT t.*
                FROM transactions t
                JOIN comptes c ON c.id = t.id_compte
                WHERE t.montant > ?
                   OR LOWER(TRIM(t.lieu)) <> LOWER(TRIM(?))
                   OR (
                       SELECT COUNT(*)
                       FROM transactions precedente
                       JOIN comptes cp
                         ON cp.id = precedente.id_compte
                       WHERE cp.id_client = c.id_client
                         AND precedente.date_transaction
                             > t.date_transaction - INTERVAL '1 minute'
                         AND (
                             precedente.date_transaction
                                 < t.date_transaction
                             OR (
                                 precedente.date_transaction
                                     = t.date_transaction
                                 AND precedente.id <= t.id
                             )
                         )
                   ) >= 3
                ORDER BY t.date_transaction DESC, t.id DESC
                """;

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setBigDecimal(1, seuil);
            statement.setString(2, lieuHabituel);
            return lireListe(statement);

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Erreur lors de la détection des transactions suspectes", e
            );
        }
    }
}