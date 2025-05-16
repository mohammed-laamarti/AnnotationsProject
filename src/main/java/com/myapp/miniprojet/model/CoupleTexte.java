package com.myapp.miniprojet.model;

import jakarta.persistence.*;
@Entity
@Table(name = "couple_textes")
public class CoupleTexte {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private String texte1;
    private String texte2;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotation_id")
    private Annotation annotation;
    @ManyToOne
    @JoinColumn(name = "tache_id")
    private Tache tache;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    private DataSet dataset;

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

    public Tache getTache() {
        return tache;
    }

    public void setTache(Tache tache) {
        this.tache = tache;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public String getTexte2() {
        return texte2;
    }

    public void setTexte2(String texte2) {
        this.texte2 = texte2;
    }

    public String getTexte1() {
        return texte1;
    }

    public void setTexte1(String texte1) {
        this.texte1 = texte1;
    }
}

