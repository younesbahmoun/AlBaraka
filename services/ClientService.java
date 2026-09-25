package services;

import model.Client;
import repository.ClientRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client ajouter(String nom, String email) {
        valider(nom, email);

        return clientRepository.ajouter(
                new Client(0, nom.trim(), email.trim())
        );
    }

    public void modifier(int id, String nom, String email) {
        validerId(id);
        valider(nom, email);

        boolean modifie = clientRepository.modifier(
                new Client(id, nom.trim(), email.trim())
        );

        if (!modifie) {
            throw new NoSuchElementException("Client introuvable : " + id);
        }
    }

    public void supprimer(int id) {
        validerId(id);

        if (!clientRepository.supprimer(id)) {
            throw new NoSuchElementException("Client introuvable : " + id);
        }
    }

    public Optional<Client> trouverParId(int id) {
        validerId(id);
        return clientRepository.trouverParId(id);
    }

    public List<Client> rechercherParNom(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom recherché est obligatoire.");
        }

        return clientRepository.rechercherParNom(nom.trim());
    }

    public List<Client> listerTous() {
        return clientRepository.listerTous();
    }

    public ClientRepository.InformationsClient informationsRapport(int clientId) {
        validerId(clientId);

        return clientRepository.informationsRapport(clientId)
                .orElseThrow(() ->
                        new NoSuchElementException("Client introuvable : " + clientId)
                );
    }

    private void valider(String nom, String email) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom est obligatoire.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("L'email est obligatoire.");
        }
    }

    private void validerId(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("L'ID doit être positif.");
        }
    }
}