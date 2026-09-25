package ui;

import services.RapportService;

import java.math.BigDecimal;
import java.util.Scanner;

public class RapportMenu {
    private final RapportService rapportService;
    private final Scanner scanner;

    public RapportMenu(RapportService rapportService, Scanner scanner) {
        this.rapportService = rapportService;
        this.scanner = scanner;
    }

    public void lancer() {
        while (true) {
            System.out.println("""
                    
                    === RAPPORTS ET STATISTIQUES ===
                    1. Top 5 des clients par solde
                    2. Rapport mensuel des transactions
                    3. Comptes inactifs
                    4. Transactions suspectes
                    0. Retour au menu principal
                    """);
            System.out.print("Votre choix : ");

            if (!scanner.hasNextLine()) {
                return;
            }

            String choix = scanner.nextLine().trim();

            if (choix.equals("0")) {
                return;
            }

            try {
                switch (choix) {
                    case "1" -> afficherTopClients();
                    case "2" -> afficherRapportMensuel();
                    case "3" -> afficherComptesInactifs();
                    case "4" -> afficherTransactionsSuspectes();
                    default -> System.out.println("Choix invalide.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Saisie invalide : " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    private void afficherTopClients() {
        var clients = rapportService.top5Clients();

        if (clients.isEmpty()) {
            System.out.println("Aucun client trouvé.");
            return;
        }

        System.out.println("\n--- TOP 5 DES CLIENTS ---");
        for (var client : clients) {
            System.out.printf(
                    "ID : %d | Nom : %s | Solde total : %s%n",
                    client.id(),
                    client.nom(),
                    client.soldeTotal()
            );
        }
    }

    private void afficherRapportMensuel() {
        int annee = lireEntier("Année : ");
        int mois = lireEntier("Mois (1 à 12) : ");

        var rapport = rapportService.rapportMensuel(annee, mois);

        if (rapport.isEmpty()) {
            System.out.println("Aucune transaction pour cette période.");
            return;
        }

        System.out.println("\n--- RAPPORT MENSUEL ---");
        for (var ligne : rapport) {
            System.out.printf(
                    "Type : %s | Nombre : %d | Volume total : %s%n",
                    ligne.type(),
                    ligne.nombreTransactions(),
                    ligne.volumeTotal()
            );
        }
    }

    private void afficherComptesInactifs() {
        int nombreMois = lireEntier("Inactifs depuis combien de mois ? ");

        var comptes = rapportService.comptesInactifs(nombreMois);

        if (comptes.isEmpty()) {
            System.out.println("Aucun compte inactif trouvé.");
            return;
        }

        System.out.println("\n--- COMPTES INACTIFS ---");
        for (var compte : comptes) {
            System.out.printf(
                    "ID : %d | Numéro : %s | Dernière transaction : %s%n",
                    compte.id(),
                    compte.numero(),
                    compte.derniereTransaction()
            );
        }
    }

    private void afficherTransactionsSuspectes() {
        System.out.print("Seuil du montant : ");
        BigDecimal seuil = new BigDecimal(scanner.nextLine().trim());

        System.out.print("Lieu habituel : ");
        String lieuHabituel = scanner.nextLine().trim();

        var transactions =
                rapportService.transactionsSuspectes(seuil, lieuHabituel);

        if (transactions.isEmpty()) {
            System.out.println("Aucune transaction suspecte trouvée.");
            return;
        }

        System.out.println("\n--- TRANSACTIONS SUSPECTES ---");
        for (var transaction : transactions) {
            System.out.printf(
                    "ID : %d | Client : %d | Montant : %s | Lieu : %s "
                            + "| Opérations dans la minute : %d%n",
                    transaction.id(),
                    transaction.idClient(),
                    transaction.montant(),
                    transaction.lieu(),
                    transaction.operationsDansLaMinute()
            );
        }
    }

    private int lireEntier(String message) {
        System.out.print(message);
        return Integer.parseInt(scanner.nextLine().trim());
    }
}