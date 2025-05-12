package com.myapp.miniprojet.Controller;

import com.myapp.miniprojet.model.Role;
import com.myapp.miniprojet.model.User;
import com.myapp.miniprojet.repository.RoleRepository;
import com.myapp.miniprojet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

//    @GetMapping("/register")
//    public String showRegisterForm(Model model) {
//        model.addAttribute("user", new User());
//        model.addAttribute("roles", roleRepository.findAll());
//        return "register";
//    }
//
//    @PostMapping("/register")
//    public String registerUser(@ModelAttribute("user") User user,
//                               @RequestParam("roleId") int roleId) {
//        Role role = roleRepository.findById(roleId)
//                .orElseThrow(() -> new IllegalArgumentException("Rôle invalide"));
//
//        user.setPassword(passwordEncoder.encode(user.getPassword()));
//        user.setRole(role);
//        userRepository.save(user);
//
//        return "redirect:/login";
//    }
}