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
    @OneToOne(mappedBy = "coupleTexte", cascade = CascadeType.ALL)
    private Annotation annotation;
    @ManyToOne
    @JoinColumn(name = "tache_id")
    private Tache tache;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id")
    private DataSet dataset;
}
