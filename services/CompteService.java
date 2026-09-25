package services;

import exception.CompteException;
import model.Compte;
import model.CompteCourant;
import model.CompteEpargne;
import repository.ClientRepository;
import repository.CompteRepository;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CompteService {
    private final CompteRepository compteRepository;
    private final ClientRepository clientRepository;

    public CompteService(
            CompteRepository compteRepository,
            ClientRepository clientRepository
    ) {
        this.compteRepository = compteRepository;
        this.clientRepository = clientRepository;
    }

    private void verifierClient(int idClient) {
        if (clientRepository.trouverParId(idClient).isEmpty()) {
            throw new CompteException("Client introuvable : " + idClient);
        }
    }

    private void verifierNumero(String numero) {
        if (numero == null || numero.isBlank() || numero.length() > 50) {
            throw new CompteException("Le numéro doit contenir entre 1 et 50 caractères.");
        }
    }

    private void verifierMontantNonNegatif(BigDecimal montant, String nom) {
        if (montant == null || montant.signum() < 0) {
            throw new CompteException(nom + " doit être positif ou égal à zéro.");
        }
    }

    private Compte compteObligatoire(long id) {
        return compteRepository.trouverParId(id)
                .orElseThrow(() -> new CompteException("Compte introuvable : " + id));
    }

    public CompteCourant creerCourant(
            String numero,
            BigDecimal solde,
            int idClient,
            BigDecimal decouvertAutorise
    ) {
        verifierClient(idClient);
        verifierNumero(numero);
        verifierMontantNonNegatif(decouvertAutorise, "Le découvert autorisé");

        if (solde == null || solde.compareTo(decouvertAutorise.negate()) < 0) {
            throw new CompteException("Le solde dépasse le découvert autorisé.");
        }

        Compte compte = new CompteCourant(
                0, numero.trim(), solde, idClient, decouvertAutorise
        );

        return (CompteCourant) compteRepository.ajouter(compte);
    }

    public CompteEpargne creerEpargne(
            String numero,
            BigDecimal solde,
            int idClient,
            BigDecimal tauxInteret
    ) {
        verifierClient(idClient);
        verifierNumero(numero);
        verifierMontantNonNegatif(solde, "Le solde");
        verifierMontantNonNegatif(tauxInteret, "Le taux d'intérêt");

        Compte compte = new CompteEpargne(
                0, numero.trim(), solde, idClient, tauxInteret
        );

        return (CompteEpargne) compteRepository.ajouter(compte);
    }

    public void modifierSolde(long id, BigDecimal nouveauSolde) {
        Compte actuel = compteObligatoire(id);
        Compte modifie;

        if (actuel instanceof CompteCourant courant) {
            if (nouveauSolde == null
                    || nouveauSolde.compareTo(
                            courant.getDecouvertAutorise().negate()
                    ) < 0) {
                throw new CompteException("Le solde dépasse le découvert autorisé.");
            }

            modifie = new CompteCourant(
                    courant.getId(),
                    courant.getNumero(),
                    nouveauSolde,
                    courant.getIdClient(),
                    courant.getDecouvertAutorise()
            );
        } else {
            CompteEpargne epargne = (CompteEpargne) actuel;
            verifierMontantNonNegatif(nouveauSolde, "Le solde");

            modifie = new CompteEpargne(
                    epargne.getId(),
                    epargne.getNumero(),
                    nouveauSolde,
                    epargne.getIdClient(),
                    epargne.getTauxInteret()
            );
        }

        compteRepository.modifier(modifie);
    }

    public void modifierDecouvert(long id, BigDecimal nouveauDecouvert) {
        Compte compte = compteObligatoire(id);

        if (!(compte instanceof CompteCourant courant)) {
            throw new CompteException("Ce compte n'est pas un compte courant.");
        }

        verifierMontantNonNegatif(nouveauDecouvert, "Le découvert autorisé");

        if (courant.getSolde().compareTo(nouveauDecouvert.negate()) < 0) {
            throw new CompteException(
                    "Le solde actuel dépasse le nouveau découvert autorisé."
            );
        }

        compteRepository.modifier(new CompteCourant(
                courant.getId(),
                courant.getNumero(),
                courant.getSolde(),
                courant.getIdClient(),
                nouveauDecouvert
        ));
    }

    public void modifierTauxInteret(long id, BigDecimal nouveauTaux) {
        Compte compte = compteObligatoire(id);

        if (!(compte instanceof CompteEpargne epargne)) {
            throw new CompteException("Ce compte n'est pas un compte épargne.");
        }

        verifierMontantNonNegatif(nouveauTaux, "Le taux d'intérêt");

        compteRepository.modifier(new CompteEpargne(
                epargne.getId(),
                epargne.getNumero(),
                epargne.getSolde(),
                epargne.getIdClient(),
                nouveauTaux
        ));
    }

    public void supprimer(long id) {
        if (!compteRepository.supprimer(id)) {
            throw new CompteException("Compte introuvable : " + id);
        }
    }

    public Optional<Compte> trouverParId(long id) {
        return compteRepository.trouverParId(id);
    }

    public Optional<Compte> trouverParNumero(String numero) {
        verifierNumero(numero);
        return compteRepository.trouverParNumero(numero.trim());
    }

    public List<Compte> rechercherParClient(int idClient) {
        verifierClient(idClient);
        return compteRepository.rechercherParClient(idClient);
    }

    public List<Compte> listerTous() {
        return compteRepository.listerTous();
    }

    public Optional<Compte> compteSoldeMaximum() {
        return compteRepository.listerTous().stream()
                .max(Comparator.comparing(Compte::getSolde));
    }

    public Optional<Compte> compteSoldeMinimum() {
        return compteRepository.listerTous().stream()
                .min(Comparator.comparing(Compte::getSolde));
    }
}