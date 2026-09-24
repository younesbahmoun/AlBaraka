package model;

import java.math.BigDecimal;

public final class CompteCourant extends Compte {

    private BigDecimal decouvertAutorise;

    public CompteCourant(
            long id,
            String numero,
            BigDecimal solde,
            long idClient,
            BigDecimal decouvertAutorise
    ) {
        super(id, numero, solde, idClient);
        this.decouvertAutorise = decouvertAutorise;
    }

    public BigDecimal getDecouvertAutorise() {
        return decouvertAutorise;
    }

    public void setDecouvertAutorise(BigDecimal decouvertAutorise) {
        this.decouvertAutorise = decouvertAutorise;
    }
}