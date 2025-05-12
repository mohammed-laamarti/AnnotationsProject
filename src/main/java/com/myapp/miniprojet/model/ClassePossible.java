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
}
