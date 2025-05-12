package com.myapp.miniprojet.model;

import jakarta.persistence.DiscriminatorValue;

@DiscriminatorValue("ADMIN")
public class Admin extends User {

}
