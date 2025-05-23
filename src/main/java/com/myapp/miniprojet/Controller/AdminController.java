package com.myapp.miniprojet.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.miniprojet.DTO.AnnotatorDTO;
import com.myapp.miniprojet.model.*;
import com.myapp.miniprojet.service.AdminService;
import com.opencsv.exceptions.CsvValidationException;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) throws JsonProcessingException {

        model.addAttribute("datasets", adminService.getAllDatasets());
        model.addAttribute("totalAnnotators", adminService.getAllAnnotators().size());
        model.addAttribute("totalAnnotationsToday", 78);
        List<Object> progressData = adminService.getDatasetProgressData();
        List<String> datasetNames = (List<String>) progressData.get(0);
        List<Integer> datasetProgress = (List<Integer>) progressData.get(1);
        System.out.println(datasetNames);
        System.out.println(datasetProgress);
//        model.addAttribute("datasetNames", datasetNames);
//        model.addAttribute("datasetProgress",  datasetProgress );
        Map<String, Integer> datasetMap = new HashMap<>();
        for (int i = 0; i < datasetNames.size(); i++) {
            datasetMap.put(datasetNames.get(i), datasetProgress.get(i));
        }

        ObjectMapper mapper = new ObjectMapper();
        String jsonData = mapper.writeValueAsString(datasetMap); // convert Map to JSON

        model.addAttribute("datasetJson", jsonData);
        List<Annotateur> topAnnotators = adminService.getTop5Annotators();
        System.out.println("Top Annotators: " + topAnnotators);
        model.addAttribute("topAnnotators", topAnnotators);
        return "admin/dashboard"; // Correspond à a.html
    }
    @GetMapping("/datasets")
    public String datasets(Model model) {
        // Récupérer les datasets et leurs progrès via adminService
        List<DataSet> datasets = adminService.getAllDatasets();
        Map<Long, Integer> datasetProgress = adminService.getDatasetProgressMap();



        // Ajouter les attributs au modèle
        model.addAttribute("title", "Gestion des Datasets");
        model.addAttribute("datasets", datasets);
        model.addAttribute("datasetProgress", datasetProgress);

        return "admin/datasets";
    }
    @PostMapping("/dataset/save")
    public String saveDataset(
            @RequestParam("nom") String nom,
            @RequestParam("classe") String classe,
            @RequestParam("description") String description,
            @RequestParam(value = "file", required = false) MultipartFile file,
            Model model) {
        DataSet dataset = new DataSet();
        dataset.setNom(nom);
        dataset.setDescription(description);

        // Traiter les classes possibles
        if (!classe.isEmpty()) {
            String[] classes = classe.split(",");
            for (String className : classes) {
                className = className.trim();
                if (!className.isEmpty()) {
                    ClassePossible classePossible = new ClassePossible();
                    classePossible.setNom(className);
                    classePossible.setDataset(dataset);
                    dataset.getClassesPossibles().add(classePossible);
                }
            }
        }

        try {
            adminService.createDataset(dataset, file);
            model.addAttribute("successMessage", "Dataset créé avec succès.");
        } catch (IOException | CsvValidationException e) {
            model.addAttribute("errorMessage", "Erreur lors de la création : " + e.getMessage());
        }
        return "redirect:/admin/datasets";
    }


    @GetMapping("/dataset/details/{id}")
    public String datasetDetails(@PathVariable Long id, Model model) {
        DataSet dataset = adminService.getAllDatasets().stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (dataset == null) {
            return "redirect:/admin/datasets"; // Rediriger si le dataset n'est pas trouvé
        }

        System.out.println(dataset.getClassesPossibles());

        // Calculer le pourcentage d'avancement
        Map<Long, Integer> datasetProgress = adminService.getDatasetProgressMap();
        int progress = datasetProgress.getOrDefault(id, 0);

        // Récupérer les annotateurs affectés
        java.util.List<com.myapp.miniprojet.model.Annotateur> annotateurs = adminService.getAnnotateursForDataset(id);

        model.addAttribute("dataset", dataset);
        model.addAttribute("progress", progress);
        model.addAttribute("coupleCount", dataset.getCouplesTextes().size());
        model.addAttribute("annotateurs", annotateurs);
        return "admin/dataset-details";
    }


//    @GetMapping("/dataset/assign/{id}")
//    public String assignAnnotators(@PathVariable Long id, Model model) {
//        DataSet dataset = adminService.findDatasetById(id);
//        if (dataset == null) {
//            return "redirect:/admin/datasets";
//        }
//        System.out.println("Dataset ID passé au modèle : " + dataset.getId());
//
//        Map<String, List<Annotateur>> annotatorsMap = adminService.getAnnotatorsForAssignment(id);
//        model.addAttribute("dataset", dataset);
//        model.addAttribute("assignedAnnotators", annotatorsMap.get("assigned"));
//        model.addAttribute("availableAnnotators", annotatorsMap.get("available"));
//        return "admin/dataset-assign";
//    }

    @PostMapping("/api/dataset/{id}/assign-annotator")
    public String assignAnnotator(@PathVariable("id") Long datasetId, @RequestParam("annotatorId") Long annotatorId, Model model) {

        try {
            adminService.assignAnnotatorToDataset(datasetId, annotatorId);
            model.addAttribute("successMessage", "Annotateur affecté avec succès.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Erreur lors de l'affectation : " + e.getMessage());
        }
        return "redirect:/admin/dataset/assign/" + datasetId;
    }

//    @PostMapping("/api/dataset/{datasetId}/remove-annotator/{annotatorId}")
//    public String removeAnnotator(@PathVariable Long datasetId, @PathVariable Long annotatorId, Model model) {
//        System.out.println("---------------------'");
//        try {
//            adminService.removeAnnotatorFromDataset(datasetId, annotatorId);
//            model.addAttribute("successMessage", "Annotateur retiré avec succès.");
//        } catch (Exception e) {
//            model.addAttribute("errorMessage", "Erreur lors du retrait : " + e.getMessage());
//        }
//        return "redirect:/admin/dataset/assign/" + datasetId;
//    }
@GetMapping("/dataset/assign/{id}")
public String showAssignAnnotators(@PathVariable Long id, Model model, HttpSession session) {
    DataSet dataset = adminService.findDatasetById(id);
    if (dataset == null) {
        model.addAttribute("errorMessage", "Dataset non trouvé.");
        return "redirect:/admin/datasets";
    }

    // Récupérer les annotateurs affectés via les tâches existantes
    List<Annotateur> assignedAnnotators = dataset.getTaches().stream()
            .map(Tache::getAnnotateur)
            .collect(Collectors.toList());
    System.out.println("Annotateurs affectés : " + assignedAnnotators);

    // Récupérer les annotateurs disponibles
    List<Annotateur> availableAnnotators = adminService.getAvailableAnnotators(dataset.getId());
    System.out.println("Annotateurs disponibles : " + availableAnnotators);

    // Récupérer la liste des annotateurs sélectionnés depuis la session
    List<Long> selectedAnnotatorIds = (List<Long>) session.getAttribute("selectedAnnotatorIds");
    if (selectedAnnotatorIds == null) {
        selectedAnnotatorIds = new ArrayList<>();
        session.setAttribute("selectedAnnotatorIds", selectedAnnotatorIds);
    }
    System.out.println("IDs des annotateurs sélectionnés : " + selectedAnnotatorIds);

    // Récupérer les annotateurs correspondants aux selectedAnnotatorIds
    List<Annotateur> selectedAnnotators = selectedAnnotatorIds.stream()
            .map(annotatorId -> {
                Annotateur annotator = adminService.findAnnotatorById(annotatorId);
                System.out.println("Recherche annotateur ID " + annotatorId + " : " + annotator);
                return annotator;
            })
            .filter(annotator -> annotator != null) // Filtrer les annotateurs non trouvés
            .collect(Collectors.toList());
    System.out.println("Annotateurs sélectionnés : " + selectedAnnotators);

    // Ajouter les attributs au modèle
    model.addAttribute("dataset", dataset);
    model.addAttribute("assignedAnnotators", assignedAnnotators);
    model.addAttribute("availableAnnotators", availableAnnotators);
    model.addAttribute("selectedAnnotators", selectedAnnotators);
    model.addAttribute("selectedAnnotatorIds", selectedAnnotatorIds);

    return "admin/dataset-assign";
}

    @PostMapping("/dataset/assign/{id}/add")
    public String addAnnotator(@PathVariable Long id, @RequestParam Long annotatorId, Model model, HttpSession session) {
        DataSet dataset = adminService.findDatasetById(id);
        if (dataset == null) {
            model.addAttribute("errorMessage", "Dataset non trouvé.");
            return "redirect:/admin/datasets";
        }

        Annotateur annotator = adminService.findAnnotatorById(annotatorId);
        System.out.println("Annotateur trouvé : " + annotator);
        if (annotator == null) {
            model.addAttribute("errorMessage", "Annotateur non trouvé avec l'ID : " + annotatorId);
            return "redirect:/admin/dataset/assign/" + id;
        }

        List<Long> selectedAnnotatorIds = (List<Long>) session.getAttribute("selectedAnnotatorIds");
        if (selectedAnnotatorIds == null) {
            selectedAnnotatorIds = new ArrayList<>();
            session.setAttribute("selectedAnnotatorIds", selectedAnnotatorIds);
        }
        if (!selectedAnnotatorIds.contains(annotatorId)) {
            selectedAnnotatorIds.add(annotatorId);
            System.out.println("Annotateur ajouté : " + annotatorId);
            adminService.assignAnnotatorToDataset(id, annotatorId);
        }

        return "redirect:/admin/dataset/assign/" + id;
    }

    @PostMapping("/dataset/assign/{id}/remove")
    public String removeAnnotator(@PathVariable Long id, @RequestParam Long annotatorId, Model model, HttpSession session) {
        DataSet dataset = adminService.findDatasetById(id);
        if (dataset == null) {
            model.addAttribute("errorMessage", "Dataset non trouvé.");
            return "redirect:/admin/datasets";
        }

        adminService.removeAnnotatorFromDataset(dataset.getId(), annotatorId);
        List<Long> selectedAnnotatorIds = (List<Long>) session.getAttribute("selectedAnnotatorIds");
        if (selectedAnnotatorIds != null) {
            selectedAnnotatorIds.remove(annotatorId);
        }
        return "redirect:/admin/dataset/assign/" + id;
    }


//    @PostMapping("/dataset/assign/{id}/remove")
//    public String removeAnnotator(@PathVariable Long id, @RequestParam Long annotatorId, Model model) {
//        DataSet dataset = adminService.findDatasetById(id);
//        if (dataset == null) {
//            model.addAttribute("errorMessage", "Dataset non trouvé.");
//            return "redirect:/admin/datasets";
//        }
//
//        // Supprimer la tâche de l'annotateur sans toucher aux annotations
//        adminService.removeAnnotatorFromDataset(dataset.getId(), annotatorId);
//        return "redirect:/admin/dataset/assign/" + id;
//    }

    @PostMapping("/dataset/assign/{id}/distribute")
    public String distributeTasks(@PathVariable Long id, Model model, HttpSession session) {
        DataSet dataset = adminService.findDatasetById(id);
        if (dataset == null) {
            model.addAttribute("errorMessage", "Dataset non trouvé.");
            return "redirect:/admin/datasets";
        }

        if (dataset.getCouplesTextes().isEmpty()) {
            model.addAttribute("errorMessage", "Aucun couple de textes disponible à répartir.");
            return "redirect:/admin/dataset/assign/" + id;
        }

        List<Long> selectedAnnotatorIds = (List<Long>) session.getAttribute("selectedAnnotatorIds");
        if (selectedAnnotatorIds != null && !selectedAnnotatorIds.isEmpty()) {
            adminService.distributeTasksToAnnotators(dataset, selectedAnnotatorIds);
            model.addAttribute("successMessage", "Tâches réparties avec succès.");
            session.setAttribute("selectedAnnotatorIds", new ArrayList<>());
        } else {
            model.addAttribute("errorMessage", "Aucun annotateur sélectionné pour la répartition.");
        }
        return "redirect:/admin/dataset/assign/" + id;
    }

    @GetMapping("/dataset/export/{id}")
    public ResponseEntity<byte[]> exportDataset(@PathVariable Long id) throws Exception {
        String csvContent = adminService.exportAnnotatedDataset(id);

        // Préparer le fichier pour le téléchargement
        byte[] csvBytes = csvContent.getBytes();
        String fileName = "dataset_annotated_" + id + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(csvBytes);
    }

//    annotateurs

    @GetMapping("/annotateurs")
    public String annotators(Model model) {
        List<Annotateur> annotators = adminService.getAllAnnotators();
        model.addAttribute("annotators", annotators);
        return "admin/annotateurs";
    }

    @GetMapping("/annotator/{id}")
    @ResponseBody
    public ResponseEntity<AnnotatorDTO> getAnnotatorById(@PathVariable Long id) {
        try {
            AnnotatorDTO annotator = adminService.getAnnotatorById(id);
            return ResponseEntity.ok(annotator);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/annotator/save")
    @ResponseBody
    public String saveAnnotator(@RequestBody Annotateur annotator) {
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println(annotator);
        adminService.addAnnotator(annotator);
        return "admin/annotateurs";
    }
//
    @PutMapping("/annotator/update/{id}")
    @ResponseBody
    public String updateAnnotator(@PathVariable Long id, @RequestBody User updatedAnnotator) {
        System.out.println("||||||||||||||||||||||||||||||||");
     adminService.updateAnnotator(id,updatedAnnotator);
        return "redirect:/admin/annotateurs";
    }

    @DeleteMapping("/annotator/delete/{id}")
    @ResponseBody
    public String deleteAnnotator(@PathVariable Long id, Model model) {
        try {
            adminService.deleteAnnotator(id);
            model.addAttribute("successMessage", "L'annotateur a été marqué comme supprimé avec succès.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "admin/annotateurs"; // Retourne la vue avec les messages
    }
@GetMapping("/logout")
public String logout(HttpSession session) {
    session.invalidate(); // Invalide la session
    return "redirect:/auth/login"; // Redirige vers une page de login (à créer si nécessaire)
}

}

