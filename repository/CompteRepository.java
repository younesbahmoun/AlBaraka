package repository;

import model.Compte;

import java.util.List;
import java.util.Optional;

public interface CompteRepository {
    Compte ajouter(Compte compte);

    boolean modifier(Compte compte);

    boolean supprimer(long id);

    Optional<Compte> trouverParId(long id);

    Optional<Compte> trouverParNumero(String numero);

    List<Compte> rechercherParClient(int idClient);

    List<Compte> listerTous();
}