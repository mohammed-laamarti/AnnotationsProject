package com.myapp.miniprojet.model;

import jakarta.persistence.*;

@Entity
public class ClassePossible {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String nom;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    private DataSet dataset;

    public ClassePossible(DataSet dataset, String nom, long id) {
        this.dataset = dataset;
        this.nom = nom;
        this.id = id;
    }

    @Override
    public String toString() {
        return "ClassePossible{" +
                "id=" + id +
                ", nom='" + nom + '\'' +

                '}';
    }

    public ClassePossible() {}
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public DataSet getDataset() {
        return dataset;
    }

    public void setDataset(DataSet dataset) {
        this.dataset = dataset;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }
}
