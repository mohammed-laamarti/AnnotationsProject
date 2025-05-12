package com.myapp.miniprojet.service;

import com.myapp.miniprojet.model.Annotateur;
import com.myapp.miniprojet.model.DataSet;
import com.myapp.miniprojet.model.Tache;
import com.myapp.miniprojet.model.User;
import com.myapp.miniprojet.repository.AnnotateurRepository;
import com.myapp.miniprojet.repository.DataSetRepository;
import com.myapp.miniprojet.repository.TacheRepository;
import com.myapp.miniprojet.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {
    @Autowired
    private AnnotateurRepository annotatorRepository;
    @Autowired
    private DataSetRepository datasetRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TacheRepository tacheRepository;

    // Récupérer tous les datasets
    public List<DataSet> getAllDatasets() {
        return datasetRepository.findAll();
    }

    // Récupérer un dataset par ID
    public DataSet getDatasetById(Long id) {
        return datasetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé : " + id));
    }

    // Créer un nouveau dataset
    public void createDataset(DataSet dataset) {
        datasetRepository.save(dataset);
    }

    // Ajouter un nouvel annotateur (utilisateur de type ANNOTATOR)
    public void addAnnotator(User annotatorData) {
        Annotateur annotator = new Annotateur();
        annotator.setNom(annotatorData.getNom());
        annotator.setPrenom(annotatorData.getPrenom());
        annotator.setLogin(annotatorData.getLogin());
        annotator.setPassword(annotatorData.getPassword());
        annotator.setRole(annotatorData.getRole()); // Assurez-vous que le rôle est défini
        userRepository.save(annotator);
    }

    // Récupérer tous les annotateurs (utilisateurs de type ANNOTATOR)
    public List<User> getAllAnnotators() {
        return annotatorRepository.findAll().stream()
                .map(annotator -> (User) annotator)
                .collect(Collectors.toList());
    }

    // Récupérer un annotateur par login
    public User getAnnotatorByLogin(String login) {
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new IllegalArgumentException("Annotateur non trouvé : " + login));
        if (!(user instanceof Annotateur)) {
            throw new IllegalArgumentException("L'utilisateur " + login + " n'est pas un annotator.");
        }
        return user;
    }

    // Récupérer les annotateurs affectés à un dataset via les tâches
    public List<User> getAnnotatorsForDataset(Long datasetId) {
        return tacheRepository.findByDatasetId(datasetId)
                .stream()
                .map(Tache::getAnnotateur)
                .collect(Collectors.toList());
    }

    // Affecter un annotateur à un dataset via une tâche
    public void assignAnnotator(Long datasetId, String annotatorLogin) {
        DataSet dataset = getDatasetById(datasetId);
        User annotator = getAnnotatorByLogin(annotatorLogin);

        // Vérifier si la tâche existe déjà
        if (tacheRepository.findByDatasetIdAndAnnotateurId(datasetId, annotator.getId()).isEmpty()) {
            Tache tache = new Tache();
            tache.setDataset(dataset);
            tache.setAnnotateur((Annotateur) annotator);
            tache.setDateLimite(new Date()); // À personnaliser si une date limite est requise
            tacheRepository.save(tache);
        }
    }

    // Désaffecter un annotateur d'un dataset
    public void unassignAnnotator(Long datasetId, String annotatorLogin) {
        User annotator = getAnnotatorByLogin(annotatorLogin);
        Tache tache = tacheRepository.findByDatasetIdAndAnnotateurId(datasetId, annotator.getId())
                .orElseThrow(() -> new IllegalArgumentException("Tache non trouvée pour cet annotateur et ce dataset"));
        tacheRepository.delete(tache); // Supprime la tâche sans affecter les annotations
    }

}
