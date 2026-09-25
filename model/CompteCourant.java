package model;

import java.math.BigDecimal;

public final class CompteCourant extends Compte {
    private final BigDecimal decouvertAutorise;

    public CompteCourant(
            long id,
            String numero,
            BigDecimal solde,
            int idClient,
            BigDecimal decouvertAutorise
    ) {
        super(id, numero, solde, idClient);
        this.decouvertAutorise = decouvertAutorise;
    }

    public BigDecimal getDecouvertAutorise() {
        return decouvertAutorise;
    }

    @Override
    public String toString() {
        return "CompteCourant{id=" + getId()
                + ", numero='" + getNumero() + '\''
                + ", solde=" + getSolde()
                + ", idClient=" + getIdClient()
                + ", decouvertAutorise=" + decouvertAutorise
                + '}';
    }
}