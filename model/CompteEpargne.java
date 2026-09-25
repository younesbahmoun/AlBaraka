package model;

import java.math.BigDecimal;

public final class CompteEpargne extends Compte {
    private final BigDecimal tauxInteret;

    public CompteEpargne(
            long id,
            String numero,
            BigDecimal solde,
            int idClient,
            BigDecimal tauxInteret
    ) {
        super(id, numero, solde, idClient);
        this.tauxInteret = tauxInteret;
    }

    public BigDecimal getTauxInteret() {
        return tauxInteret;
    }

    @Override
    public String toString() {
        return "CompteEpargne{id=" + getId()
                + ", numero='" + getNumero() + '\''
                + ", solde=" + getSolde()
                + ", idClient=" + getIdClient()
                + ", tauxInteret=" + tauxInteret
                + '}';
    }
}