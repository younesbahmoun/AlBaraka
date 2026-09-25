package main;

import repository.ClientRepository;
import repository.CompteRepository;
import repository.jdbc.JdbcClientRepository;
import repository.jdbc.JdbcCompteRepository;
import services.ClientService;
import services.CompteService;
import services.RapportService;
import services.TransactionService;
import ui.ClientMenu;
import ui.CompteMenu;
import ui.RapportMenu;
import ui.TransactionMenu;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/AlBaraka";
        String user = "postgres";
        String password = "yns1234";

        ClientRepository clientRepository =
                new JdbcClientRepository(url, user, password);

        CompteRepository compteRepository =
                new JdbcCompteRepository(url, user, password);

        ClientService clientService =
                new ClientService(clientRepository);

        CompteService compteService =
                new CompteService(compteRepository, clientRepository);

        TransactionService transactionService =
                new TransactionService(url, user, password);

        RapportService rapportService =
                new RapportService(url, user, password);

        try (Scanner scanner = new Scanner(System.in)) {
            ClientMenu clientMenu =
                    new ClientMenu(clientService, scanner);

            CompteMenu compteMenu =
                    new CompteMenu(compteService, scanner);

            TransactionMenu transactionMenu =
                    new TransactionMenu(transactionService, scanner);

            RapportMenu rapportMenu =
                    new RapportMenu(rapportService, scanner);

            while (true) {
                System.out.println("""
                        
                        === MENU PRINCIPAL ===
                        1. Gestion des clients
                        2. Gestion des comptes
                        3. Gestion des transactions
                        4. Rapports et statistiques
                        0. Quitter
                        """);
                System.out.print("Votre choix : ");

                if (!scanner.hasNextLine()) {
                    return;
                }

                switch (scanner.nextLine().trim()) {
                    case "1" -> clientMenu.lancer();
                    case "2" -> compteMenu.lancer();
                    case "3" -> transactionMenu.lancer();
                    case "4" -> rapportMenu.lancer();
                    case "0" -> {
                        System.out.println("Au revoir !");
                        return;
                    }
                    default -> System.out.println("Choix invalide.");
                }
            }
        }
    }
}