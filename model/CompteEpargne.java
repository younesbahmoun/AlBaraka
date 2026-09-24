package model;

import java.math.BigDecimal;

public final class CompteEpargne extends Compte {

    private BigDecimal tauxInteret;

    public CompteEpargne(
            long id,
            String numero,
            BigDecimal solde,
            long idClient,
            BigDecimal tauxInteret
    ) {
        super(id, numero, solde, idClient);
        this.tauxInteret = tauxInteret;
    }

    public BigDecimal getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(BigDecimal tauxInteret) {
        this.tauxInteret = tauxInteret;
    }
}