package ui;

import exception.RepositoryException;
import model.Client;
import repository.ClientRepository;
import services.ClientService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientMenu {
    private final ClientService clientService;
    private final Scanner scanner;

    public ClientMenu(ClientService clientService, Scanner scanner) {
        this.clientService = clientService;
        this.scanner = scanner;
    }

    public void lancer() {
        while (true) {
            afficherMenu();
            if (!scanner.hasNextLine()) {
                return;
            }
            String choix = scanner.nextLine().trim();

            try {
                switch (choix) {
                    case "1" -> ajouter();
                    case "2" -> modifier();
                    case "3" -> supprimer();
                    case "4" -> rechercherParId();
                    case "5" -> rechercherParNom();
                    case "6" -> listerTous();
                    case "7" -> afficherRapport();
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Choix invalide.");
                }
            } catch (IllegalArgumentException | NoSuchElementException e) {
                System.out.println("Erreur : " + e.getMessage());
            } catch (RepositoryException e) {
                System.out.println("Erreur de base de données : " + e.getMessage());

                // Shows the actual SQL error while you are developing.
                if (e.getCause() != null) {
                    System.out.println("Détail : " + e.getCause().getMessage());
                }
            }
        }
    }

    private void afficherMenu() {
        System.out.println("""
                
                === GESTION DES CLIENTS ===
                1. Ajouter un client
                2. Modifier un client
                3. Supprimer un client
                4. Rechercher par ID
                5. Rechercher par nom
                6. Lister tous les clients
                7. Informations pour le rapport
                0. Retour au menu principal
                """);

        System.out.print("Votre choix : ");
    }

    private void ajouter() {
        System.out.print("Nom : ");
        String nom = scanner.nextLine();

        System.out.print("Email : ");
        String email = scanner.nextLine();

        Client client = clientService.ajouter(nom, email);
        System.out.println("Client ajouté : " + client);
    }

    private void modifier() {
        int id = lireId();

        System.out.print("Nouveau nom : ");
        String nom = scanner.nextLine();

        System.out.print("Nouvel email : ");
        String email = scanner.nextLine();

        clientService.modifier(id, nom, email);
        System.out.println("Client modifié.");
    }

    private void supprimer() {
        int id = lireId();

        clientService.supprimer(id);
        System.out.println("Client supprimé.");
    }

    private void rechercherParId() {
        int id = lireId();

        clientService.trouverParId(id).ifPresentOrElse(
                System.out::println,
                () -> System.out.println("Client introuvable.")
        );
    }

    private void rechercherParNom() {
        System.out.print("Nom recherché : ");
        String nom = scanner.nextLine();

        afficherClients(clientService.rechercherParNom(nom));
    }

    private void listerTous() {
        afficherClients(clientService.listerTous());
    }

    private void afficherRapport() {
        int id = lireId();
        ClientRepository.InformationsClient infos =
                clientService.informationsRapport(id);

        System.out.println("Nombre de comptes : " + infos.nombreComptes());
        System.out.println("Solde total : " + infos.soldeTotal());
    }

    private void afficherClients(List<Client> clients) {
        if (clients.isEmpty()) {
            System.out.println("Aucun client trouvé.");
            return;
        }

        for (Client client : clients) {
            System.out.println(client);
        }
    }

    private int lireId() {
        System.out.print("ID du client : ");
        return Integer.parseInt(scanner.nextLine().trim());
    }
}