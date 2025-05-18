package com.myapp.miniprojet;

import com.myapp.miniprojet.model.*;
import com.myapp.miniprojet.repository.DataSetRepository;
import com.myapp.miniprojet.repository.RoleRepository;
import com.myapp.miniprojet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private DataSetRepository dataSetRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        initRoles();
        initAdminUser();
        initAnnotateurs();
//        initDatasets();
    }

    private void initRoles() {
        if (roleRepository.count() == 0) {
            Role adminRole = new Role();
            adminRole.setRole("ADMIN");
            roleRepository.save(adminRole);

            Role annotateurRole = new Role();
            annotateurRole.setRole("ANNOTATEUR");
            roleRepository.save(annotateurRole);
        }
    }

    private void initAdminUser() {
        if (!userRepository.existsByLogin("admin")) {
            Role adminRole = roleRepository.findByRole("ADMIN");

            Admin admin = new Admin();
            admin.setNom("Admin");
            admin.setPrenom("System");
            admin.setLogin("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(adminRole);

            userRepository.save(admin);
        }
    }

    private void initAnnotateurs() {
        String[] noms = {"Dupont", "Martin", "Bernard"};
        String[] prenoms = {"Jean", "Marie", "Pierre"};

        Role annotRole = roleRepository.findByRole("ANNOTATEUR");

        for (int i = 0; i < 3; i++) {
            String login = "annot" + (i + 1);
            if (!userRepository.existsByLogin(login)) {
                Annotateur annotateur = new Annotateur();
                annotateur.setNom(noms[i]);
                annotateur.setPrenom(prenoms[i]);
                annotateur.setLogin(login);
                annotateur.setPassword(passwordEncoder.encode("annot" + (i + 1) + "pass"));
                annotateur.setRole(annotRole);

                userRepository.save(annotateur);
            }
        }
    }

    private void initDatasets() {
        if (dataSetRepository.count() == 0) {
            String[] datasetNoms = {"Dataset Actualités", "Dataset Réseaux Sociaux", "Dataset Commentaires"};
            String[] datasetDescriptions = {
                    "Données collectées à partir d'articles de presse.",
                    "Données extraites des réseaux sociaux.",
                    "Commentaires d'utilisateurs sur diverses plateformes."
            };

            for (int i = 0; i < datasetNoms.length; i++) {
                if (!dataSetRepository.existsByNom(datasetNoms[i])) {
                    DataSet dataset = new DataSet();
                    dataset.setNom(datasetNoms[i]);
                    dataset.setDescription(datasetDescriptions[i]);

                    // Ajouter des classes possibles
                    ClassePossible classe1 = new ClassePossible();
                    classe1.setNom("Positif");
                    classe1.setDataset(dataset);

                    ClassePossible classe2 = new ClassePossible();
                    classe2.setNom("Négatif");
                    classe2.setDataset(dataset);

                    dataset.getClassesPossibles().addAll(Arrays.asList(classe1, classe2));

                    // Ajouter des tâches
                    for (User user : userRepository.findAllAnnotateurs()) {
                        Annotateur annotateur = (Annotateur) user;
                        Tache tache = new Tache();
                        tache.setDataset(dataset);
                        tache.setAnnotateur(annotateur);
                        dataset.getTaches().add(tache);
                        annotateur.getTaches().add(tache);
                        userRepository.save(annotateur);
                    }

                    // Ajouter des couples textes
                    for (Tache tache : dataset.getTaches()) {
                        CoupleTexte coupleTexte = new CoupleTexte();
                        coupleTexte.setTexte1("Ceci est un texte positif " + (i + 1));
                        coupleTexte.setTexte2("Ceci est un texte négatif " + (i + 1));
                        coupleTexte.setDataset(dataset);
                        coupleTexte.setTache(tache);

                        // Créer une annotation pour ce couple texte
                        Annotation annotation = new Annotation();
                        annotation.setAnnotateur(tache.getAnnotateur());
                        annotation.setCoupleTexte(coupleTexte);
                        coupleTexte.setAnnotation(annotation);

                        tache.getAnnotateur().getAnnotations().add(annotation);

                        dataset.getCouplesTextes().add(coupleTexte);
                    }

                    dataSetRepository.save(dataset);
                }
            }
        }
    }
}