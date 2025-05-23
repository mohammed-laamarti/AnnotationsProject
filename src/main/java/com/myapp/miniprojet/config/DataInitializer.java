package com.myapp.miniprojet.config;

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
        String[] noms = {"Dupont", "Martin", "Bernard","laamarti","azeroual","ouchajaa"};
        String[] prenoms = {"Jean", "Marie", "Pierre","mohammed","hicham","amine"};

        Role annotRole = roleRepository.findByRole("ANNOTATEUR");

        for (int i = 0; i < 6; i++) {
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

}