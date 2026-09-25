package model;

import java.math.BigDecimal;

public abstract sealed class Compte permits CompteCourant, CompteEpargne {
    private final long id;
    private final String numero;
    private final BigDecimal solde;
    private final int idClient;

    protected Compte(long id, String numero, BigDecimal solde, int idClient) {
        this.id = id;
        this.numero = numero;
        this.solde = solde;
        this.idClient = idClient;
    }

    public long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public int getIdClient() {
        return idClient;
    }
}