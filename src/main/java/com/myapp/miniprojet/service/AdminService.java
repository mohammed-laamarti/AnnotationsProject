package com.myapp.miniprojet.service;

import com.myapp.miniprojet.dto.AnnotatorDTO;
import com.myapp.miniprojet.model.*;
import com.myapp.miniprojet.repository.DataSetRepository;
import com.myapp.miniprojet.repository.RoleRepository;
import com.myapp.miniprojet.repository.TacheRepository;
import com.myapp.miniprojet.repository.UserRepository;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private DataSetRepository datasetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TacheRepository tacheRepository;
//    @Autowired
//    private DataSetService dataSetService;
    @Autowired
    private RoleRepository roleRepository;

    @Transactional
    public void updateAnnotator(Long id, User annotatorData) {
        Optional<User> annotatorOptional = userRepository.findById(id);
        if (!annotatorOptional.isPresent()) {
            throw new IllegalArgumentException("Annotateur non trouvé avec l'ID : " + id);
        }

        User existingAnnotator = annotatorOptional.get();
        if (!(existingAnnotator instanceof Annotateur)) {
            throw new IllegalArgumentException("L'utilisateur avec l'ID " + id + " n'est pas un annotateur.");
        }

        if (annotatorData.getNom() != null && !annotatorData.getNom().isEmpty()) {
            existingAnnotator.setNom(annotatorData.getNom());
        }
        if (annotatorData.getPrenom() != null && !annotatorData.getPrenom().isEmpty()) {
            existingAnnotator.setPrenom(annotatorData.getPrenom());
        }
        if (annotatorData.getLogin() != null && !annotatorData.getLogin().isEmpty()) {
            User userWithLogin = userRepository.findByLogin(annotatorData.getLogin()).orElse(null);
            if (userWithLogin != null && userWithLogin.getId() != id.longValue()) {
                throw new IllegalArgumentException("Le login " + annotatorData.getLogin() + " est déjà utilisé.");
            }
            existingAnnotator.setLogin(annotatorData.getLogin());
        }

        userRepository.save(existingAnnotator);
    }
    @Transactional
    public void deleteAnnotator(Long id) {
        Optional<User> annotatorOptional = userRepository.findById(id);
        if (!annotatorOptional.isPresent()) {
            throw new IllegalArgumentException("Annotateur non trouvé avec l'ID : " + id);
        }
        User annotator = annotatorOptional.get();
        if (!(annotator instanceof Annotateur)) {
            throw new IllegalArgumentException("L'utilisateur avec l'ID " + id + " n'est pas un annotateur.");
        }
        // Supprimer l'annotateur de la base de données
        userRepository.deleteById(id);
    }


    /**
     * Ajoute un nouvel annotateur.
     *
     * @param annotatorData Données de l'annotateur
     */
    @Transactional
    public void addAnnotator(User annotatorData) {
        if (annotatorData == null || annotatorData.getLogin() == null || annotatorData.getRole() == null) {
            throw new IllegalArgumentException("Les données de l'annotateur sont invalides");
        }
        Role role = roleRepository.findByRole("ANNOTATEUR");
        Annotateur annotator = new Annotateur();
        annotator.setNom(annotatorData.getNom());
        annotator.setPrenom(annotatorData.getPrenom());
        annotator.setLogin(annotatorData.getLogin());
        // Générer un mot de passe aléatoire basé sur UUID, tronqué à 10 caractères
        String randomPassword = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);
        annotator.setPassword(randomPassword);
        annotator.setRole(role);
        userRepository.save(annotator);
    }

    /**
     * Récupère un annotateur par login.
     *
     * @param login Login de l'annotateur
     * @return Annotateur correspondant
     */
    @Transactional(readOnly = true)
    public User getAnnotatorByLogin(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Annotateur non trouvé : " + login));
        if (!(user instanceof Annotateur)) {
            throw new IllegalArgumentException("L'utilisateur " + login + " n'est pas un annotateur.");
        }
        return user;
    }

    /**
     * Récupère les annotateurs affectés à un dataset via les tâches.
     *
     * @param datasetId ID du dataset
     * @return Liste des annotateurs affectés
     */
    @Transactional(readOnly = true)
    public List<User> getAnnotatorsForDataset(Long datasetId) {
        List<Tache> taches = tacheRepository.findByDatasetId(datasetId);
        taches.forEach(tache -> Hibernate.initialize(tache.getAnnotateur()));
        return taches.stream()
                .map(Tache::getAnnotateur)
                .collect(Collectors.toList());
    }

    /**
     * Affecte un annotateur à un dataset via une tâche.
     *
     * @param datasetId ID du dataset
     * @param annotatorLogin Login de l'annotateur
     */
//    @Transactional
//    public void assignAnnotator(Long datasetId, String annotatorLogin) {
//        DataSet dataset = dataSetService.getDatasetById(datasetId);
//        User annotator = getAnnotatorByLogin(annotatorLogin);
//
//        // Vérifier si la tâche existe déjà
//        if (tacheRepository.findByDatasetIdAndAnnotateurId(datasetId, annotator.getId()).isEmpty()) {
//            Tache tache = new Tache();
//            tache.setDataset(dataset);
//            tache.setAnnotateur((Annotateur) annotator);
//            tache.setDateLimite(new Date());
//            tacheRepository.save(tache);
//        }
//    }

    /**
//     * Désaffecte un annotateur d'un dataset.
//     *
//     * @param datasetId ID du dataset
//     * @param annotatorLogin Login de l'annotateur
//     */
//    @Transactional
//    public void unassignAnnotator(Long datasetId, String annotatorLogin) {
//        User annotator = getAnnotatorByLogin(annotatorLogin);
//        Tache tache = tacheRepository.findByDatasetIdAndAnnotateurId(datasetId, annotator.getId())
//                .orElseThrow(() -> new IllegalArgumentException("Tache non trouvée pour cet annotateur et ce dataset"));
//        tacheRepository.delete(tache);
//    }
    @Transactional(readOnly = true)
    public List<Annotateur> getAllAnnotators() {
        List<Annotateur> annotators = userRepository.findAllAnnotateurs();
        annotators.forEach(annotator -> Hibernate.initialize(annotator.getAnnotations()));
        return annotators.stream()
                .map(user -> (Annotateur) user)
                .collect(Collectors.toList());
    }

    /**
     * Récupère les 5 annotateurs ayant le plus d'annotations.
     *
     * @return Liste des 5 meilleurs annotateurs
     */
    @Transactional(readOnly = true)
    public List<Annotateur> getTop5Annotators() {
        List<Annotateur> annotators = getAllAnnotators();
        annotators.sort((a, b) -> {
            int sizeA = (a.getAnnotations() != null) ? a.getAnnotations().size() : 0;
            int sizeB = (b.getAnnotations() != null) ? b.getAnnotations().size() : 0;
            return sizeB - sizeA;
        });
        if (annotators.size() > 5) {
            return annotators.subList(0, 5);
        }
        return annotators;
    }
    @Transactional(readOnly = true)
    public AnnotatorDTO getAnnotatorById(Long id) {
        Optional<User> userOptional = userRepository.findById(id);
        if (!userOptional.isPresent()) {
            throw new IllegalArgumentException("Annotateur non trouvé avec l'ID : " + id);
        }
        User user = userOptional.get();
        if (!(user instanceof Annotateur)) {
            throw new IllegalArgumentException("L'utilisateur avec l'ID " + id + " n'est pas un annotateur.");
        }
        return new AnnotatorDTO((Annotateur) user);
    }
}