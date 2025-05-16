package com.myapp.miniprojet.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Entity
@Table(name = "taches")
public class Tache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    private Date dateLimite;
    @ManyToOne
    @JoinColumn(name = "dataset_id") // Clé étrangère
    private DataSet dataset;
    @OneToMany(mappedBy = "tache", cascade = CascadeType.ALL)
    private List<CoupleTexte> couplesTextes = new ArrayList<>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "annotateur_id")
    private Annotateur annotateur;
    private int progress;          // Ajouté pour correspondre à la vue
    private int size;              // Ajouté pour correspondre à la vue
    private int progressPercentage;
    @Override
    public String toString() {
        return "Tache{" +
                "id=" + id +
                ", dateLimite=" + dateLimite +
                '}';
    }

    public Annotateur getAnnotateur() {
        return annotateur;
    }

    public void setAnnotateur(Annotateur annotateur) {
        this.annotateur = annotateur;
    }

    public List<CoupleTexte> getCouplesTextes() {
        return couplesTextes;
    }

    public void setCouplesTextes(List<CoupleTexte> couplesTextes) {
        this.couplesTextes = couplesTextes;
    }

    public DataSet getDataset() {
        return dataset;
    }

    public void setDataset(DataSet dataset) {
        this.dataset = dataset;
    }

    public Date getDateLimite() {
        return dateLimite;
    }

    public void setDateLimite(Date dateLimite) {
        this.dateLimite = dateLimite;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(int progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
