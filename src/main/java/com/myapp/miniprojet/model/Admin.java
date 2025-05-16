package com.myapp.miniprojet.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {
    public Admin(long id, String nom, String prenom, String login, String password, Role role) {
        super(id, nom, prenom, login, password, role);
    }
    public Admin(){}
}
