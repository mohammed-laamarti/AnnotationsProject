package com.myapp.miniprojet.model;


import jakarta.persistence.*;

@Entity
@Table(name = "annotations")
public class Annotation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String classChoisi;

    @ManyToOne
    @JoinColumn(name = "annotateur_id") // Clé étrangère vers la table Annotateur
    private Annotateur annotateur;

    @ManyToOne
    @JoinColumn(name = "couple_texte_id")
    private CoupleTexte coupleTexte;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CoupleTexte getCoupleTexte() {
        return coupleTexte;
    }

    public void setCoupleTexte(CoupleTexte coupleTexte) {
        this.coupleTexte = coupleTexte;
    }

    public Annotateur getAnnotateur() {
        return annotateur;
    }

    public void setAnnotateur(Annotateur annotateur) {
        this.annotateur = annotateur;
    }

    public String getClassChoisi() {
        return classChoisi;
    }

    public void setClassChoisi(String classChoisi) {
        this.classChoisi = classChoisi;
    }
}
