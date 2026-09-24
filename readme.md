Where does each check go?
Rule or error	 -  Place
Montant must be positive  - 	TransactionService
Account must exist  -	TransactionService, using CompteDAO
Withdrawal must respect solde/découvert	 -  TransactionService
Save balances and transfer together	 -  JDBC transaction, coordinated by the service
SQL connection/query fails	-  DAO reports the failure
Show a readable error to the user	-  UI/menu catches the error