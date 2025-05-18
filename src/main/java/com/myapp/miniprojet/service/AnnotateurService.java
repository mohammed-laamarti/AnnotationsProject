package com.myapp.miniprojet.service;

import com.myapp.miniprojet.model.*;
import com.myapp.miniprojet.repository.AnnotationRepository;
import com.myapp.miniprojet.repository.TacheRepository;
import org.hibernate.Hibernate;
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

    public List<Tache> getTasksForAnnotator(Long annotateurId) {
        List<Tache> tasks = tacheRepository.findByAnnotateurId(annotateurId);
        for (Tache task : tasks) {
            Hibernate.initialize(task.getCouplesTextes());
            if (task.getCouplesTextes() == null || task.getCouplesTextes().isEmpty()) {
                System.out.println("Aucun CoupleTexte trouvé pour la tâche ID : " + task.getId());
            } else {
                System.out.println("Tâche ID : " + task.getId() + " a " + task.getCouplesTextes().size() + " couples");
            }
        }
        return tasks;
    }

    public Tache getTaskById(Long taskId) {
        Tache task = tacheRepository.findById(taskId).orElse(null);
        if (task != null) {
            Hibernate.initialize(task.getCouplesTextes());
            Hibernate.initialize(task.getDataset().getClassesPossibles());
        }
        return task;
    }

    @Transactional
    public boolean saveAnnotation(Long taskId, int index, String annotation, Long annotateurId) {
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
                int annotatedCount = (int) couples.stream()
                        .filter(c -> c.getAnnotation() != null && c.getAnnotation().getClassChoisi() != null)
                        .count();
                task.setProgress(annotatedCount);
                task.setSize(couples.size());
                task.setProgressPercentage((int) ((annotatedCount / (double) couples.size()) * 100));

                tacheRepository.save(task);
                System.out.println("Annotation sauvegardée pour la paire à l'index " + index);

                // Vérifier si toutes les paires sont annotées
                return annotatedCount == couples.size();
            } else {
                throw new IllegalArgumentException("Index invalide : " + index);
            }
        } catch (Exception e) {
            System.err.println("Erreur dans saveAnnotation: " + e.getMessage());
            throw e;
        }
    }

    public boolean markTaskAsCompleted(Long taskId, Long userId) {
        Tache task = getTaskById(taskId);
        if (task == null || !(task.getAnnotateur().getId()==userId)) {
            return false;
        }

        // Vérifier si toutes les paires ont une annotation
        boolean allAnnotated = task.getCouplesTextes().stream()
                .allMatch(couple -> couple.getAnnotation() != null && couple.getAnnotation().getClassChoisi() != null);

        if (allAnnotated) {
            task.setProgress(task.getSize());
            task.setProgressPercentage(100);
            tacheRepository.save(task);
            return true;
        }
        return false;
    }
}