package com.myapp.miniprojet.Controller;

import com.myapp.miniprojet.model.CoupleTexte;
import com.myapp.miniprojet.model.Tache;
import com.myapp.miniprojet.service.AnnotateurService;
import com.myapp.miniprojet.Security.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/annotateur")
public class AnnotateurController {

    @Autowired
    private AnnotateurService annotateurService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @GetMapping("/accueil")
    public String home(Model model, Principal principal) {
        try {
            String login = principal.getName();
            System.out.println("Login de l'utilisateur : " + login);
            Long userId = userDetailsService.getUserByLogin(login).getId();
            System.out.println("User ID récupéré : " + userId);
            List<Tache> tasks = annotateurService.getTasksForAnnotator(userId);
            System.out.println("Nombre de tâches récupérées : " + (tasks != null ? tasks.size() : "null"));
            tasks.forEach(System.out::println);
            model.addAttribute("login", login);
            model.addAttribute("tasks", tasks);
            return "annotateur/acceuil";
        } catch (Exception e) {
            System.err.println("Erreur dans home: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Une erreur est survenue : " + e.getMessage());
            return "annotateur/acceuil";
        }
    }

    @GetMapping("/task/{id}/work")
    public String workOnTask(@PathVariable Long id, Model model, Principal principal) {
        System.out.println("'''''''''''''''''''''''");
        System.out.println(id);
        try {
            String login = principal.getName();
            Long userId = userDetailsService.getUserByLogin(login).getId();
            Tache task = annotateurService.getTaskById(id);
            if (task == null || !(task.getAnnotateur().getId()==userId)) {
                model.addAttribute("errorMessage", "Tâche introuvable ou non assignée à cet annotateur.");
                return "annotateur/acceuil";
            }

            if (task.getCouplesTextes().isEmpty()) {
                model.addAttribute("errorMessage", "Aucune paire de textes à annoter pour cette tâche.");
                return "annotateur/acceuil";
            }

            // Récupérer les classes possibles pour ce dataset
            List<String> possibleClasses = task.getDataset().getClassesPossibles().stream()
                    .map(classe -> classe.getNom())
                    .toList();
            model.addAttribute("task", task);
            model.addAttribute("index", 0);
            model.addAttribute("possibleClasses", possibleClasses);
            return "annotateur/work";
        } catch (Exception e) {
            System.err.println("Erreur dans workOnTask: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Une erreur est survenue : " + e.getMessage());
            return "annotateur/acceuil";
        }
    }

    @PostMapping("/task/{id}/work")
    public String saveAnnotation(@PathVariable Long id, @RequestParam("annotation") String annotation,
                                 @RequestParam("index") int index, Model model, Principal principal) {
        System.out.println("-------------------------------------");
        try {
            String login = principal.getName();
            Long userId = userDetailsService.getUserByLogin(login).getId();
            Tache task = annotateurService.getTaskById(id);
            if (task == null || !(task.getAnnotateur().getId()==userId)) {
                model.addAttribute("errorMessage", "Tâche introuvable ou non assignée à cet annotateur.");
                return "annotateur/accueil";
            }

            // Sauvegarder l'annotation et vérifier si la tâche est terminée
            boolean isTaskCompleted = annotateurService.saveAnnotation(id, index, annotation, userId);

            List<CoupleTexte> couples = task.getCouplesTextes();
            if (index + 1 < couples.size()) {
                // Recharger les classes possibles
                List<String> possibleClasses = task.getDataset().getClassesPossibles().stream()
                        .map(classe -> classe.getNom())
                        .toList();
                model.addAttribute("task", task);
                model.addAttribute("index", index + 1);
                model.addAttribute("possibleClasses", possibleClasses);
                model.addAttribute("successMessage", "Annotation sauvegardée avec succès.");
                return "annotateur/work";
            } else {
                // Vérifier si la tâche est terminée
                if (isTaskCompleted) {
                    annotateurService.markTaskAsCompleted(id, userId);
                    model.addAttribute("successMessage", "Félicitations ! Vous avez terminé toutes les annotations de cette tâche.");
                } else {
                    model.addAttribute("errorMessage", "Certaines annotations sont manquantes. Veuillez vérifier.");
                }
                return "redirect:/annotateur/accueil";
            }
        } catch (Exception e) {
            System.err.println("Erreur dans saveAnnotation: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Une erreur est survenue : " + e.getMessage());
            return "annotateur/accueil";
        }
    }
}
