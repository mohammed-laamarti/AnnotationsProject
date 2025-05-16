package com.myapp.miniprojet.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
@DiscriminatorValue("ANNOTATEUR")
public class Annotateur extends User {

    @OneToMany(mappedBy = "annotateur", cascade = CascadeType.ALL)
    private List<Annotation> annotations = new ArrayList<>();
    @OneToMany(mappedBy = "annotateur", fetch = FetchType.LAZY)
    private List<Tache> taches;

    public Annotateur(long id, String nom, String prenom, String login, String password, Role role, List<Annotation> annotations, List<Tache> taches) {
        super(id, nom, prenom, login, password, role);
        this.annotations = annotations;
        this.taches = taches;
    }
    public Annotateur(){
    }

    public Annotateur(User annotator) {
        super(annotator.getId(), annotator.getNom(), annotator.getPrenom(), annotator.getLogin(), annotator.getPassword(), annotator.getRole());

    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(List<Annotation> annotations) {
        this.annotations = annotations;
    }

    public List<Tache> getTaches() {
        return taches;
    }

    public void setTaches(List<Tache> taches) {
        this.taches = taches;
    }

    public int getTextesAnnotesCount() {
        return annotations.size(); // Assume que chaque annotation = 1 texte annoté
    }

    public Annotateur(long id, String nom, String prenom, String login, String password, Role role) {
        super(id, nom, prenom, login, password, role);
    }

}
