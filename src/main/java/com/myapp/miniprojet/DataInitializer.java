package com.myapp.miniprojet;

import com.myapp.miniprojet.model.Role;
import com.myapp.miniprojet.model.User;
import com.myapp.miniprojet.repository.RoleRepository;
import com.myapp.miniprojet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
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
            Role adminRole = roleRepository.findByRole("ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role ADMIN non trouvé"));

            User admin = new User();
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

        Role annotRole = roleRepository.findByRole("ANNOTATEUR")
                .orElseThrow(() -> new RuntimeException("Role ANNOTATEUR non trouvé"));

        for (int i = 0; i < 3; i++) {
            String login = "annot" + (i + 1);
            if (!userRepository.existsByLogin(login)) {
                User annotateur = new User();
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
