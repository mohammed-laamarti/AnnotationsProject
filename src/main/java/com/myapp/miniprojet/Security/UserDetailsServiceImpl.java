package com.myapp.miniprojet.Security;

import com.myapp.miniprojet.model.User;
import com.myapp.miniprojet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + login));

        // Obtenir le rôle de l'utilisateur
        String role = user.getRole().getRole(); // Assume que User a une méthode getRole() renvoyant un Role
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role); // Spring Security attend "ROLE_" devant les rôles

        // Convertir en UserDetails avec le rôle dynamique
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getLogin())
                .password(user.getPassword())
                .authorities(Collections.singletonList(authority))
                .build();
    }

    // Méthode pour récupérer l'entité User à partir du login
    public User getUserByLogin(String login) {
        return userRepository.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + login));
    }
}