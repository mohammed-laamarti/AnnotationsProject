package com.myapp.miniprojet.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/annotateur")
public class user {

    @GetMapping("/accueil")
    public String home(Model model) {
        return "user";
    }
}
