package ui;

import enums.TypeTransaction;
import services.TransactionService;
import services.TransactionService.LigneTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class TransactionMenu {
    private final TransactionService service;
    private final Scanner scanner;

    public TransactionMenu(TransactionService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void lancer() {
        while (true) {
            System.out.println("""
                    
                    === GESTION DES TRANSACTIONS ===
                    1. Lister les transactions d'un client
                    2. Lister les transactions d'un compte
                    3. Filtrer les transactions d'un client
                    4. Regrouper par type
                    5. Regrouper par mois
                    6. Statistiques d'un client
                    7. Statistiques d'un compte
                    8. Détecter les transactions suspectes
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
                    case "1" -> listerParClient();
                    case "2" -> listerParCompte();
                    case "3" -> filtrer();
                    case "4" -> regrouperParType();
                    case "5" -> regrouperParMois();
                    case "6" -> statistiquesClient();
                    case "7" -> statistiquesCompte();
                    case "8" -> detecterSuspectes();
                    default -> System.out.println("Choix invalide.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Saisie invalide : " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }

    private void listerParClient() {
        int idClient = lireInt("ID du client : ");
        afficherTransactions(service.listerParClient(idClient));
    }

    private void listerParCompte() {
        long idCompte = lireLong("ID du compte : ");
        afficherTransactions(service.listerParCompte(idCompte));
    }

    private void filtrer() {
        int idClient = lireInt("ID du client : ");

        System.out.println("Laisse vide un filtre que tu ne veux pas utiliser.");

        BigDecimal minimum = lireMontantOptionnel("Montant minimum : ");
        BigDecimal maximum = lireMontantOptionnel("Montant maximum : ");

        System.out.print("Type (VERSEMENT, RETRAIT, VIREMENT) : ");
        String saisieType = scanner.nextLine().trim();
        TypeTransaction type = saisieType.isEmpty()
                ? null
                : TypeTransaction.valueOf(
                        saisieType.toUpperCase(Locale.ROOT)
                );

        System.out.print("Date (AAAA-MM-JJ) : ");
        String saisieDate = scanner.nextLine().trim();
        LocalDate date = saisieDate.isEmpty()
                ? null
                : LocalDate.parse(saisieDate);

        System.out.print("Lieu : ");
        String lieu = scanner.nextLine().trim();

        afficherTransactions(service.filtrer(
                idClient, minimum, maximum, type, date, lieu
        ));
    }

    private void regrouperParType() {
        int idClient = lireInt("ID du client : ");
        afficherGroupes(service.regrouperParType(idClient));
    }

    private void regrouperParMois() {
        int idClient = lireInt("ID du client : ");
        afficherGroupes(service.regrouperParMois(idClient));
    }

    private void statistiquesClient() {
        int idClient = lireInt("ID du client : ");
        afficherStatistiques(service.statistiquesClient(idClient));
    }

    private void statistiquesCompte() {
        long idCompte = lireLong("ID du compte : ");
        afficherStatistiques(service.statistiquesCompte(idCompte));
    }

    private void detecterSuspectes() {
        System.out.print("Seuil du montant : ");
        BigDecimal seuil = new BigDecimal(scanner.nextLine().trim());

        System.out.print("Lieu habituel : ");
        String lieuHabituel = scanner.nextLine().trim();

        afficherTransactions(
                service.detecterSuspectes(seuil, lieuHabituel)
        );
    }

    private void afficherTransactions(List<LigneTransaction> transactions) {
        if (transactions.isEmpty()) {
            System.out.println("Aucune transaction trouvée.");
            return;
        }

        for (LigneTransaction transaction : transactions) {
            System.out.printf(
                    "ID : %d | Date : %s | Type : %s | Montant : %s"
                            + " | Lieu : %s | Compte : %d | Destination : %s%n",
                    transaction.id(),
                    transaction.date(),
                    transaction.type(),
                    transaction.montant(),
                    transaction.lieu(),
                    transaction.idCompte(),
                    transaction.idCompteDestination() == null
                            ? "-"
                            : transaction.idCompteDestination()
            );
        }
    }

    private void afficherGroupes(List<TransactionService.Groupe> groupes) {
        if (groupes.isEmpty()) {
            System.out.println("Aucune transaction trouvée.");
            return;
        }

        for (var groupe : groupes) {
            System.out.printf(
                    "%s | Nombre : %d | Total : %s%n",
                    groupe.valeur(),
                    groupe.nombre(),
                    groupe.total()
            );
        }
    }

    private void afficherStatistiques(
            TransactionService.Statistiques statistiques
    ) {
        System.out.println("Nombre : " + statistiques.nombre());
        System.out.println("Total : " + statistiques.total());
        System.out.println("Moyenne : " + statistiques.moyenne());
    }

    private int lireInt(String message) {
        System.out.print(message);
        return Integer.parseInt(scanner.nextLine().trim());
    }

    private long lireLong(String message) {
        System.out.print(message);
        return Long.parseLong(scanner.nextLine().trim());
    }

    private BigDecimal lireMontantOptionnel(String message) {
        System.out.print(message);
        String saisie = scanner.nextLine().trim();

        return saisie.isEmpty() ? null : new BigDecimal(saisie);
    }
}