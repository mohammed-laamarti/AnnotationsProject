package com.myapp.miniprojet.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Définir le titre
        model.addAttribute("title", "Tableau de bord Administrateur");

        // Simuler la liste des datasets
        List<DataSet> datasets = new ArrayList<>();
        datasets.add(new DataSet(1L, "Dataset 1", "Description du Dataset 1", null, null, null));
        datasets.add(new DataSet(2L, "Dataset 2", "Description du Dataset 2", null, null, null));
        model.addAttribute("datasets", datasets);

        // Simuler le nombre total d'annotateurs
        model.addAttribute("totalAnnotators", 5);

        // Simuler le progrès par dataset
        Map<Long, Integer> datasetProgress = new HashMap<>();
        datasetProgress.put(1L, 40); // 40% de progrès pour Dataset 1
        datasetProgress.put(2L, 75); // 75% de progrès pour Dataset 2
        model.addAttribute("datasetProgress", datasetProgress);

        // Simuler la liste des annotateurs
        List<User> annotators = new ArrayList<>();
        annotators.add(new User(1L, "Jean", "Dupont", "jean.dupont", "password", new Role()));
        annotators.add(new User(2L, "Marie", "Durand", "marie.durand", "password", new Role()));
        model.addAttribute("annotators", annotators);

        // Simuler la liste des classes
        List<String> classes = new ArrayList<>();
        classes.add("Class 1");
        classes.add("Class 2");
        classes.add("Class 3");
        model.addAttribute("classes", classes);

        // Ajouter un message de simulation
        model.addAttribute("message", "simo");

        // Retourner le template
        return "admin/a"; // Correspond à a.html
    }
}

// Classes simulées
class DataSet {
    private Long id;
    private String nom;
    private String description;
    private String field1;
    private String field2;
    private String field3;

    public DataSet() {}

    public DataSet(Long id, String nom, String description, String field1, String field2, String field3) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public String getDescription() { return description; }
}

class User {
    private Long id;
    private String prenom;
    private String nom;
    private String login;
    private String password;
    private Role role;

    public User() {}

    public User(Long id, String prenom, String nom, String login, String password, Role role) {
        this.id = id;
        this.prenom = prenom;
        this.nom = nom;
        this.login = login;
        this.password = password;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getPrenom() { return prenom; }
    public String getNom() { return nom; }
    public String getLogin() { return login; }
    public String getPassword() { return password; }
}

class Role {
    // Classe vide pour la simulation (à remplacer par une entité réelle plus tard)
}