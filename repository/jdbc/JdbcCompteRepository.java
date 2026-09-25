package repository.jdbc;

import exception.RepositoryException;
import model.Compte;
import model.CompteCourant;
import model.CompteEpargne;
import repository.CompteRepository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCompteRepository implements CompteRepository {
    private final String url;
    private final String user;
    private final String password;

    public JdbcCompteRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection ouvrirConnexion() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private void remplirParametres(PreparedStatement statement, Compte compte)
            throws SQLException {
        statement.setString(1, compte.getNumero());
        statement.setBigDecimal(2, compte.getSolde());
        statement.setInt(3, compte.getIdClient());

        if (compte instanceof CompteCourant courant) {
            statement.setString(4, "COURANT");
            statement.setBigDecimal(5, courant.getDecouvertAutorise());
            statement.setNull(6, java.sql.Types.NUMERIC);
        } else if (compte instanceof CompteEpargne epargne) {
            statement.setString(4, "EPARGNE");
            statement.setNull(5, java.sql.Types.NUMERIC);
            statement.setBigDecimal(6, epargne.getTauxInteret());
        }
    }

    private Compte lireCompte(ResultSet result) throws SQLException {
        long id = result.getLong("id");
        String numero = result.getString("numero");
        int idClient = result.getInt("id_client");

        return switch (result.getString("type_compte")) {
            case "COURANT" -> new CompteCourant(
                    id,
                    numero,
                    result.getBigDecimal("solde"),
                    idClient,
                    result.getBigDecimal("decouvert_autorise")
            );
            case "EPARGNE" -> new CompteEpargne(
                    id,
                    numero,
                    result.getBigDecimal("solde"),
                    idClient,
                    result.getBigDecimal("taux_interet")
            );
            default -> throw new SQLException("Type de compte inconnu pour l'ID " + id);
        };
    }

    @Override
    public Compte ajouter(Compte compte) {
        String sql = """
                INSERT INTO comptes
                    (numero, solde, id_client, type_compte,
                     decouvert_autorise, taux_interet)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            remplirParametres(statement, compte);

            try (ResultSet result = statement.executeQuery()) {
                result.next();
                long id = result.getLong("id");

                if (compte instanceof CompteCourant courant) {
                    return new CompteCourant(
                            id,
                            courant.getNumero(),
                            courant.getSolde(),
                            courant.getIdClient(),
                            courant.getDecouvertAutorise()
                    );
                }

                CompteEpargne epargne = (CompteEpargne) compte;
                return new CompteEpargne(
                        id,
                        epargne.getNumero(),
                        epargne.getSolde(),
                        epargne.getIdClient(),
                        epargne.getTauxInteret()
                );
            }

        } catch (SQLException e) {
            throw new RepositoryException("Impossible d'ajouter le compte", e);
        }
    }

    @Override
    public boolean modifier(Compte compte) {
        String sql = """
                UPDATE comptes
                SET numero = ?,
                    solde = ?,
                    id_client = ?,
                    type_compte = ?,
                    decouvert_autorise = ?,
                    taux_interet = ?
                WHERE id = ?
                """;

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            remplirParametres(statement, compte);
            statement.setLong(7, compte.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Impossible de modifier le compte " + compte.getId(), e
            );
        }
    }

    @Override
    public boolean supprimer(long id) {
        String sql = "DELETE FROM comptes WHERE id = ?";

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Impossible de supprimer le compte " + id, e
            );
        }
    }

    @Override
    public Optional<Compte> trouverParId(long id) {
        String sql = "SELECT * FROM comptes WHERE id = ?";

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? Optional.of(lireCompte(result))
                        : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Impossible de rechercher le compte " + id, e
            );
        }
    }

    @Override
    public Optional<Compte> trouverParNumero(String numero) {
        String sql = "SELECT * FROM comptes WHERE numero = ?";

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, numero);

            try (ResultSet result = statement.executeQuery()) {
                return result.next()
                        ? Optional.of(lireCompte(result))
                        : Optional.empty();
            }

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Impossible de rechercher le compte " + numero, e
            );
        }
    }

    @Override
    public List<Compte> rechercherParClient(int idClient) {
        String sql = "SELECT * FROM comptes WHERE id_client = ? ORDER BY id";
        List<Compte> comptes = new ArrayList<>();

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, idClient);

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    comptes.add(lireCompte(result));
                }
            }

            return comptes;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Impossible de rechercher les comptes du client " + idClient, e
            );
        }
    }

    @Override
    public List<Compte> listerTous() {
        String sql = "SELECT * FROM comptes ORDER BY id";
        List<Compte> comptes = new ArrayList<>();

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                comptes.add(lireCompte(result));
            }

            return comptes;

        } catch (SQLException e) {
            throw new RepositoryException("Impossible de lister les comptes", e);
        }
    }
}