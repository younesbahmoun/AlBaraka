package ui;

import exception.CompteException;
import exception.RepositoryException;
import model.Compte;
import services.CompteService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class CompteMenu {
    private final CompteService service;
    private final Scanner scanner;

    public CompteMenu(CompteService service, Scanner scanner) {
        this.service = service;
        this.scanner = scanner;
    }

    public void lancer() {
        while (true) {
            System.out.println("""
                    
                    === GESTION DES COMPTES ===
                    1. Créer un compte courant
                    2. Créer un compte épargne
                    3. Modifier le solde
                    4. Modifier le découvert autorisé
                    5. Modifier le taux d'intérêt
                    6. Rechercher par numéro
                    7. Rechercher par client
                    8. Lister tous les comptes
                    9. Compte avec solde maximum
                    10. Compte avec solde minimum
                    11. Supprimer un compte
                    0. Retour
                    """);
            System.out.print("Votre choix : ");

            if (!scanner.hasNextLine()) {
                return;
            }

            String choix = scanner.nextLine().trim();

            try {
                switch (choix) {
                    case "1" -> creerCourant();
                    case "2" -> creerEpargne();
                    case "3" -> modifierSolde();
                    case "4" -> modifierDecouvert();
                    case "5" -> modifierTaux();
                    case "6" -> rechercherParNumero();
                    case "7" -> rechercherParClient();
                    case "8" -> afficher(service.listerTous());
                    case "9" -> System.out.println(
                            service.compteSoldeMaximum()
                                    .map(Object::toString)
                                    .orElse("Aucun compte.")
                    );
                    case "10" -> System.out.println(
                            service.compteSoldeMinimum()
                                    .map(Object::toString)
                                    .orElse("Aucun compte.")
                    );
                    case "11" -> supprimer();
                    case "0" -> {
                        return;
                    }
                    default -> System.out.println("Choix invalide.");
                }
            } catch (CompteException | NumberFormatException e) {
                System.out.println("Erreur : " + e.getMessage());
            } catch (RepositoryException e) {
                System.out.println("Erreur de base de données : " + e.getMessage());
                if (e.getCause() != null) {
                    System.out.println("Détail : " + e.getCause().getMessage());
                }
            }
        }
    }

    private String lireTexte(String question) {
        System.out.print(question);
        return scanner.nextLine();
    }

    private int lireClientId() {
        return Integer.parseInt(lireTexte("ID du client : ").trim());
    }

    private long lireCompteId() {
        return Long.parseLong(lireTexte("ID du compte : ").trim());
    }

    private BigDecimal lireDecimal(String question) {
        return new BigDecimal(lireTexte(question).trim());
    }

    private void creerCourant() {
        String numero = lireTexte("Numéro : ");
        int idClient = lireClientId();
        BigDecimal solde = lireDecimal("Solde initial : ");
        BigDecimal decouvert = lireDecimal("Découvert autorisé : ");

        System.out.println("Compte créé : "
                + service.creerCourant(numero, solde, idClient, decouvert));
    }

    private void creerEpargne() {
        String numero = lireTexte("Numéro : ");
        int idClient = lireClientId();
        BigDecimal solde = lireDecimal("Solde initial : ");
        BigDecimal taux = lireDecimal("Taux d'intérêt : ");

        System.out.println("Compte créé : "
                + service.creerEpargne(numero, solde, idClient, taux));
    }

    private void modifierSolde() {
        long id = lireCompteId();
        BigDecimal solde = lireDecimal("Nouveau solde : ");
        service.modifierSolde(id, solde);
        System.out.println("Solde modifié.");
    }

    private void modifierDecouvert() {
        long id = lireCompteId();
        BigDecimal decouvert = lireDecimal("Nouveau découvert autorisé : ");
        service.modifierDecouvert(id, decouvert);
        System.out.println("Découvert modifié.");
    }

    private void modifierTaux() {
        long id = lireCompteId();
        BigDecimal taux = lireDecimal("Nouveau taux d'intérêt : ");
        service.modifierTauxInteret(id, taux);
        System.out.println("Taux modifié.");
    }

    private void rechercherParNumero() {
        String numero = lireTexte("Numéro recherché : ");
        System.out.println(
                service.trouverParNumero(numero)
                        .map(Object::toString)
                        .orElse("Compte introuvable.")
        );
    }

    private void rechercherParClient() {
        afficher(service.rechercherParClient(lireClientId()));
    }

    private void supprimer() {
        service.supprimer(lireCompteId());
        System.out.println("Compte supprimé.");
    }

    private void afficher(List<Compte> comptes) {
        if (comptes.isEmpty()) {
            System.out.println("Aucun compte trouvé.");
            return;
        }

        comptes.forEach(System.out::println);
    }
}