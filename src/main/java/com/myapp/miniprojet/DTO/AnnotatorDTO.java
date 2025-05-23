package com.myapp.miniprojet.DTO;

import com.myapp.miniprojet.model.Annotateur;

public class AnnotatorDTO {
    private Long id;
    private String nom;
    private String prenom;
    private String login;

    public AnnotatorDTO(Annotateur annotator) {
        this.id = annotator.getId();
        this.nom = annotator.getNom();
        this.prenom = annotator.getPrenom();
        this.login = annotator.getLogin();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
}
