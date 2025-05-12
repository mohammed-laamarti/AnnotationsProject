package com.myapp.miniprojet.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "datasets")
public class DataSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String description;
    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassePossible> classesPossibles = new ArrayList<>();
    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL)
    private List<Tache> taches = new ArrayList<>();
    @OneToMany(mappedBy = "dataset", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoupleTexte> couplesTextes = new ArrayList<>();


    public DataSet(Long id, String nom, String description, List<ClassePossible> classesPossibles, List<Tache> taches, List<CoupleTexte> couplesTextes) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.classesPossibles = classesPossibles;
        this.taches = taches;
        this.couplesTextes = couplesTextes;
    }

    public List<CoupleTexte> getCouplesTextes() {
        return couplesTextes;
    }

    public void setCouplesTextes(List<CoupleTexte> couplesTextes) {
        this.couplesTextes = couplesTextes;
    }

    public List<Tache> getTaches() {
        return taches;
    }

    public void setTaches(List<Tache> taches) {
        this.taches = taches;
    }

    public List<ClassePossible> getClassesPossibles() {
        return classesPossibles;
    }

    public void setClassesPossibles(List<ClassePossible> classesPossibles) {
        this.classesPossibles = classesPossibles;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
