package com.myapp.miniprojet.service;

import com.myapp.miniprojet.model.Annotateur;
import com.myapp.miniprojet.model.Annotation;
import com.myapp.miniprojet.model.CoupleTexte;
import com.myapp.miniprojet.model.Tache;
import com.myapp.miniprojet.repository.AnnotationRepository;
import com.myapp.miniprojet.repository.TacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AnnotateurService {

    @Autowired
    private TacheRepository tacheRepository;

    @Autowired
    private AnnotationRepository annotationRepository;

    @Transactional(readOnly = true)
    public List<Tache> getTasksForAnnotator(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("L'ID de l'utilisateur ne peut pas être null");
        }
        try {
            List<Tache> tasks = tacheRepository.findByAnnotateurId(userId);
            System.out.println("Tâches trouvées pour userId " + userId + ": " + tasks);
            return tasks;
        } catch (Exception e) {
            System.err.println("Erreur dans getTasksForAnnotator: " + e.getMessage());
            throw e;
        }
    }


    @Transactional(readOnly = true)
    public Tache getTaskById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID de la tâche ne peut pas être null");
        }
        try {
            Tache task = tacheRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Tâche avec id " + id + " non trouvée"));
            System.out.println("Tâche récupérée : " + task);
            return task;
        } catch (Exception e) {
            System.err.println("Erreur dans getTaskById: " + e.getMessage());
            throw e;
        }
    }


    @Transactional
    public void saveAnnotation(Long taskId, int index, String annotation, Long annotateurId) {
        try {
            Tache task = tacheRepository.findById(taskId)
                    .orElseThrow(() -> new IllegalArgumentException("Tâche avec id " + taskId + " non trouvée"));
            List<CoupleTexte> couples = task.getCouplesTextes();
            if (index >= 0 && index < couples.size()) {
                CoupleTexte couple = couples.get(index);

                // Vérifier ou créer l'annotation
                Annotation existingAnnotation = couple.getAnnotation();
                if (existingAnnotation == null) {
                    existingAnnotation = new Annotation();
                    existingAnnotation.setCoupleTexte(couple);
                    Annotateur annotateur = new Annotateur();
                    annotateur.setId(annotateurId);
                    existingAnnotation.setAnnotateur(annotateur);
                    couple.setAnnotation(existingAnnotation);
                }
                existingAnnotation.setClassChoisi(annotation);

                // Sauvegarder l'annotation
                annotationRepository.save(existingAnnotation);

                // Mettre à jour l'avancement
                int annotatedCount = (int) couples.stream().filter(c -> c.getAnnotation() != null && c.getAnnotation().getClassChoisi() != null).count();
                task.setProgress(annotatedCount);
                task.setSize(couples.size());
                task.setProgressPercentage((int) ((annotatedCount / (double) couples.size()) * 100));

                tacheRepository.save(task);
                System.out.println("Annotation sauvegardée pour la paire à l'index " + index);
            } else {
                throw new IllegalArgumentException("Index invalide : " + index);
            }
        } catch (Exception e) {
            System.err.println("Erreur dans saveAnnotation: " + e.getMessage());
            throw e;
        }
    }
}