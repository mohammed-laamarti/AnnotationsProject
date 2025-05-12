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

    @OneToOne
    @JoinColumn(name = "couple_texte_id", unique = true) // Contrainte d'unicité
    private CoupleTexte coupleTexte;

}
