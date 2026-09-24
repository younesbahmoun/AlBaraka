package model;

import java.math.BigDecimal;

public abstract sealed class Compte permits CompteCourant, CompteEpargne {

    private long id;
    private String numero;
    private BigDecimal solde;
    private long idClient;

    protected Compte(long id, String numero, BigDecimal solde, long idClient) {
        this.id = id;
        this.numero = numero;
        this.solde = solde;
        this.idClient = idClient;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public BigDecimal getSolde() {
        return solde;
    }

    public void setSolde(BigDecimal solde) {
        this.solde = solde;
    }

    public long getIdClient() {
        return idClient;
    }

    public void setIdClient(long idClient) {
        this.idClient = idClient;
    }
}