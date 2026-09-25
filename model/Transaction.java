package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import enums.TypeTransaction;

public record Transaction(
    long id,
    LocalDateTime dateTransaction,
    BigDecimal montant,
    TypeTransaction type,
    String lieu,
    long idCompte,
    Long idCompteDestination
) {}