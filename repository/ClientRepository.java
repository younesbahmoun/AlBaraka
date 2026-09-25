package repository;

import model.Client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    Client ajouter(Client client);

    boolean modifier(Client client);

    boolean supprimer(int id);

    Optional<Client> trouverParId(int id);

    List<Client> rechercherParNom(String nom);

    List<Client> listerTous();

    Optional<InformationsClient> informationsRapport(int clientId);

    // Result of the report query. It stays in this file.
    record InformationsClient(long nombreComptes, BigDecimal soldeTotal) {}
}