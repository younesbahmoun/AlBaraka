# AlBaraka — Gestion bancaire en Java

Application console de gestion bancaire développée en **Java** avec **JDBC** et **PostgreSQL**. Elle permet de consulter les clients, les comptes et les transactions, puis de produire des rapports à partir des données enregistrées en base.

## Fonctionnalités

Le menu principal donne accès à quatre rubriques :

1. **Clients** : ajout, modification, suppression, recherche et liste des clients.
2. **Comptes** : gestion des comptes courants et d'épargne, recherche et consultation des soldes.
3. **Transactions** : liste par client ou par compte, filtres (montant, type, date, lieu), regroupement par type ou par mois, total, moyenne et détection des transactions suspectes.
4. **Rapports et statistiques** : top 5 des clients par solde total, rapport mensuel des transactions, comptes inactifs et transactions suspectes.

Les transactions suspectes sont recherchées selon un seuil de montant, un lieu habituel fourni au lancement de la recherche et au moins trois opérations du même client en moins d'une minute. Ces contrôles sont des règles de démonstration, pas un système de détection de fraude.

## Technologies

- Java 17 ou version plus récente
- PostgreSQL
- JDBC et pilote PostgreSQL JDBC
- Interface en ligne de commande (`Scanner`)

## Organisation du code

```text
src/
├── main/Main.java                 # Point d'entrée et menu principal
├── model/                         # Clients, comptes et transactions
├── enums/                         # Types de comptes et de transactions
├── repository/                    # Interfaces d'accès aux données
├── repository/jdbc/               # Implémentations JDBC
├── services/                      # ClientService, CompteService,
│                                  # TransactionService, RapportService
├── ui/                            # ClientMenu, CompteMenu,
│                                  # TransactionMenu, RapportMenu
└── exception/                     # Exceptions du projet
```

L'arborescence exacte peut varier selon l'organisation de votre projet Java. `TransactionService` et `RapportService` exécutent directement des requêtes JDBC dans la version présentée ici ; les services client et compte s'appuient sur leurs repositories.

## Base de données

Créer une base PostgreSQL nommée `AlBaraka`, puis exécuter le script de création des tables du projet : `clients`, `comptes` et `transactions`.

```sql
CREATE DATABASE "AlBaraka";
```

Si vous utilisez `psql`, connectez-vous ensuite à cette base avant d'exécuter les `CREATE TABLE` et `CREATE INDEX` du projet :

```sql
\c "AlBaraka"
```

Relations principales : un client possède plusieurs comptes ; une transaction référence un compte source et, pour un virement, un compte destinataire. Les types de transaction acceptés par la base sont `VERSEMENT`, `RETRAIT` et `VIREMENT`. Les paramètres `decouvert_autorise` et `taux_interet` dépendent du type de compte.

Pour tester les menus, insérer d'abord les clients, ensuite les comptes, puis les transactions. Des données de test comprenant 10 clients, 10 comptes et 10 transactions peuvent être utilisées dans cet ordre.

## Configuration et exécution

1. Démarrer PostgreSQL et vérifier que la base `AlBaraka` et ses tables existent.
2. Ajouter le pilote **PostgreSQL JDBC** aux dépendances ou aux *Referenced Libraries* du projet VS Code. Sans ce pilote, la connexion renvoie `No suitable driver found`.
3. Dans `src/main/Main.java`, adapter les trois valeurs de connexion à votre installation :

   ```java
   String url = "jdbc:postgresql://localhost:5432/AlBaraka";
   String user = "postgres";
   String password = "VOTRE_MOT_DE_PASSE";
   ```

4. Exécuter `main.Main` avec le bouton **Run** de VS Code, ou avec l'outil de lancement Java de votre IDE.

Ne publiez pas votre mot de passe PostgreSQL dans un dépôt public. Si le projet utilise un fichier de configuration local pour les identifiants, gardez ce fichier hors de Git.

## Exemples de vérification

```sql
SELECT id, nom, email FROM clients ORDER BY id;
SELECT id, numero, solde, type_compte FROM comptes ORDER BY id;
SELECT id, date_transaction, montant, type_transaction, lieu
FROM transactions
ORDER BY date_transaction, id;
```

Dans le menu des transactions, les filtres vides sont ignorés. Le **total** et la **moyenne** portent sur les montants des opérations ; ils ne représentent pas le solde bancaire d'un compte. Pour les virements, le compte source et le compte destinataire sont affichés dans les listes par compte.

## Limites connues

- La recherche des comptes inactifs dans la version actuelle de `RapportService` ne retourne pas les comptes sans aucune transaction : sa condition `HAVING MAX(t.date_transaction) < ?` écarte les valeurs `NULL`.
- Les soldes de données de test doivent correspondre aux opérations insérées. L'insertion manuelle d'une transaction SQL ne met pas automatiquement à jour `comptes.solde`.
- La détection par lieu compare toutes les transactions au lieu habituel saisi pour la recherche ; aucun lieu habituel propre à chaque client n'est stocké dans le schéma présenté.
