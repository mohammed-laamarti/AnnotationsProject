package com.myapp.miniprojet.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
@Entity
@DiscriminatorValue("ANNOTATEUR")
public class Annotateur extends User {
    @OneToMany(mappedBy = "annotateur", cascade = CascadeType.ALL)
    private List<Annotation> annotations = new ArrayList<>();
    @OneToMany(mappedBy = "annotateur", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tache> taches = new ArrayList<>();
}
