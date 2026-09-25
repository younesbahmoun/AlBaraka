package repository.jdbc;

import exception.RepositoryException;
import model.Client;
import repository.ClientRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcClientRepository implements ClientRepository {
    private final String url;
    private final String user;
    private final String password;

    public JdbcClientRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection ouvrirConnexion() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public Client ajouter(Client client) {
        String sql = "INSERT INTO clients (nom, email) VALUES (?, ?) RETURNING id";

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, client.getNom());
            statement.setString(2, client.getEmail());

            try (ResultSet result = statement.executeQuery()) {
                result.next();
                int id = result.getInt("id");
                return new Client(id, client.getNom(), client.getEmail());
            }

        } catch (SQLException e) {
            throw new RepositoryException("Impossible d'ajouter le client", e);
        }
    }

    @Override
    public boolean modifier(Client client) {
        String sql = "UPDATE clients SET nom = ?, email = ? WHERE id = ?";

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, client.getNom());
            statement.setString(2, client.getEmail());
            statement.setInt(3, client.getId());

            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Impossible de modifier le client avec l'ID " + client.getId(), e
            );
        }
    }

    @Override
    public boolean supprimer(int id) {
        String sql = "DELETE FROM clients WHERE id = ?";

        try (Connection connection = ouvrirConnexion();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;

        } catch (SQLException e) {
            throw new RepositoryException(
                "Impossible de supprimer le client avec l'ID " + id, e
            );
        }
    }

    @Override
    public Optional<Client> trouverParId(int id) {
        String sql = "SELECT id, nom, email FROM clients WHERE id = ?";

        try (Connection connection = ouvrirConnexion();
            PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                return Optional.of(new Client(
                        result.getInt("id"),
                        result.getString("nom"),
                        result.getString("email")
                ));
            }

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Impossible de rechercher le client avec l'ID " + id, e
            );
        }
    }

    @Override
    public List<Client> rechercherParNom(String nom) {
        String sql = """
                SELECT id, nom, email
                FROM clients
                WHERE nom ILIKE ?
                ORDER BY nom, id
                """;

        List<Client> clients = new ArrayList<>();

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + nom + "%");

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    clients.add(new Client(
                            result.getInt("id"),
                            result.getString("nom"),
                            result.getString("email")
                    ));
                }
            }

            return clients;

        } catch (SQLException e) {
            throw new RepositoryException("Impossible de rechercher les clients par nom", e);
        }
    }

    @Override
    public List<Client> listerTous() {
        String sql = "SELECT id, nom, email FROM clients ORDER BY id";
        List<Client> clients = new ArrayList<>();

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                clients.add(new Client(
                        result.getInt("id"),
                        result.getString("nom"),
                        result.getString("email")
                ));
            }

            return clients;

        } catch (SQLException e) {
            throw new RepositoryException("Impossible de lister les clients", e);
        }
    }

    @Override
    public Optional<InformationsClient> informationsRapport(int clientId) {
        String sql = """
                SELECT c.id,
                       COUNT(co.id) AS nombre_comptes,
                       COALESCE(SUM(co.solde), 0) AS solde_total
                FROM clients c
                LEFT JOIN comptes co ON co.id_client = c.id
                WHERE c.id = ?
                GROUP BY c.id
                """;

        try (Connection connection = ouvrirConnexion();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, clientId);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }

                return Optional.of(new InformationsClient(
                        result.getLong("nombre_comptes"),
                        result.getBigDecimal("solde_total")
                ));
            }

        } catch (SQLException e) {
            throw new RepositoryException(
                    "Impossible de calculer les informations du client " + clientId, e
            );
        }
    }
}