package model;

public class Client {
    private final int id;
    private final String nom;
    private final String email;

    public Client(int id, String nom, String email) {
        this.id = id;
        this.nom = nom;
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getNom() {
        return nom;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return "Client{id=" + id
                + ", nom='" + nom + '\''
                + ", email='" + email + '\''
                + '}';
    }
}