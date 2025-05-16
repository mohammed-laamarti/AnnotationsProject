package com.myapp.miniprojet.service;

import com.myapp.miniprojet.model.*;
import com.myapp.miniprojet.repository.AnnotateurRepository;
import com.myapp.miniprojet.repository.DataSetRepository;
import com.myapp.miniprojet.repository.TacheRepository;
import com.myapp.miniprojet.repository.UserRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DataSetService {
    @Autowired
    private DataSetRepository datasetRepository;

    @Autowired
    private AdminService adminService;
    @Autowired
    private AnnotateurRepository annotateurRepository;
    @Autowired
    private TacheRepository tacheRepository;
    @Autowired
    private UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Object> getDatasetProgressData() {
        // Récupérer les datasets
        List<DataSet> datasets = datasetRepository.findAll();

        // Forcer le chargement des relations
        datasets.forEach(dataset -> {
            Hibernate.initialize(dataset.getCouplesTextes());
            dataset.getCouplesTextes().forEach(couple -> Hibernate.initialize(couple.getAnnotation()));
        });

        // Récupérer les noms des datasets
        List<String> datasetNames = datasets.stream()
                .map(DataSet::getNom)
                .collect(Collectors.toList());

        // Calculer le progrès de chaque dataset (pourcentage de couples textes annotés)
        List<Integer> datasetProgress = datasets.stream()
                .map(dataset -> {
                    int totalCouples = dataset.getCouplesTextes().size();
                    if (totalCouples == 0) return 0;
                    long annotatedCouples = dataset.getCouplesTextes().stream()
                            .filter(couple -> couple.getAnnotation() != null)
                            .count();
                    return (int) ((annotatedCouples * 100) / totalCouples);
                })
                .collect(Collectors.toList());

        // Retourner les deux listes dans une liste commune
        return List.of(datasetNames, datasetProgress);
    }
    @Transactional(readOnly = true)
    public Map<Long, Integer> getDatasetProgressMap() {
        // Récupérer les datasets
        List<DataSet> datasets = datasetRepository.findAll();

        // Forcer le chargement des relations
        datasets.forEach(dataset -> {
            Hibernate.initialize(dataset.getCouplesTextes());
            dataset.getCouplesTextes().forEach(couple -> Hibernate.initialize(couple.getAnnotation()));
        });

        // Créer une map associant les ID des datasets à leur pourcentage de progression
        Map<Long, Integer> datasetProgress = new HashMap<>();
        for (DataSet dataset : datasets) {
            int totalCouples = dataset.getCouplesTextes().size();
            if (totalCouples == 0) {
                datasetProgress.put(dataset.getId(), 0);
            } else {
                long annotatedCouples = dataset.getCouplesTextes().stream()
                        .filter(couple -> couple.getAnnotation() != null)
                        .count();
                datasetProgress.put(dataset.getId(), (int) ((annotatedCouples * 100) / totalCouples));
            }
        }

        return datasetProgress;
    }


    /**
     * Récupère tous les datasets.
     *
     * @return Liste des datasets
     */
    @Transactional(readOnly = true)
    public List<DataSet> getAllDatasets() {
        List<DataSet> datasets = datasetRepository.findAll();
        datasets.forEach(dataset -> Hibernate.initialize(dataset.getCouplesTextes()));
        return datasets;
    }

    /**
     * Récupère un dataset par ID.
     *
     * @param id ID du dataset
     * @return Dataset correspondant
     */
    @Transactional(readOnly = true)
    public DataSet getDatasetById(Long id) {
        DataSet dataset = datasetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Dataset non trouvé : " + id));
        Hibernate.initialize(dataset.getCouplesTextes());
        return dataset;
    }

    @Transactional
    public void createDataset(DataSet dataset, MultipartFile file) throws IOException, CsvValidationException {
        if (dataset.getCouplesTextes() == null) {
            dataset.setCouplesTextes(new ArrayList<>());
        }
        if (dataset.getClassesPossibles() == null) {
            dataset.setClassesPossibles(new ArrayList<>());
        }

        // Sauvegarder le dataset d'abord pour obtenir un ID
        dataset = datasetRepository.save(dataset);
        final DataSet persistentDataset = dataset;

        System.out.println("Dataset sauvegardé avec ID : " + persistentDataset.getId());

        if (file != null && !file.isEmpty()) {
            try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
                String[] headers = csvReader.readNext();
                if (headers == null || headers.length != 2 || !headers[0].equals("texte1") || !headers[1].equals("texte2")) {
                    throw new IOException("Le fichier CSV doit avoir exactement deux colonnes : texte1 et texte2");
                }
                List<CoupleTexte> allCouples = new ArrayList<>();
                String[] row;
                while ((row = csvReader.readNext()) != null) {
                    if (row.length == 2) {
                        CoupleTexte coupleTexte = new CoupleTexte();
                        coupleTexte.setTexte1(row[0]);
                        coupleTexte.setTexte2(row[1]);
                        coupleTexte.setDataset(persistentDataset);
                        allCouples.add(coupleTexte);
                    }
                }
                persistentDataset.setCouplesTextes(allCouples);

                // Mélanger les couples texte
                Collections.shuffle(allCouples);

                // Sauvegarder les couples texte
                datasetRepository.save(persistentDataset);

                // Répartir les couples entre annotateurs
                List<Annotateur> annotateurs = userRepository.findAllAnnotateurs();
                int totalCouples = allCouples.size();
                int annotatorsCount = annotateurs.size();
                int couplesPerAnnotator = (int) Math.ceil((double) totalCouples / annotatorsCount);

                System.out.println("Total couples : " + totalCouples + ", Annotateurs : " + annotatorsCount + ", Couples par annotateur : " + couplesPerAnnotator);

                for (int i = 0; i < annotateurs.size(); i++) {
                    Annotateur annotateur = annotateurs.get(i);
                    Tache tache = tacheRepository.findByAnnotateurIdAndDatasetId(annotateur.getId(), persistentDataset.getId())
                            .orElseGet(() -> {
                                Tache newTache = new Tache();
                                newTache.setAnnotateur(annotateur);
                                newTache.setDataset(persistentDataset);
                                newTache.setProgress(0);
                                newTache.setProgressPercentage(0);
                                newTache.setCouplesTextes(new ArrayList<>());
                                return newTache;
                            });

                    // Répartir les couples texte
                    int startIndex = i * couplesPerAnnotator;
                    int endIndex = Math.min(startIndex + couplesPerAnnotator, totalCouples);
                    List<CoupleTexte> assignedCouples = allCouples.subList(startIndex, endIndex);
                    tache.getCouplesTextes().clear(); // Vider la liste existante
                    for (CoupleTexte ct : assignedCouples) {
                        ct.setTache(tache); // Définir la relation bidirectionnelle
                        tache.getCouplesTextes().add(ct);
                    }
                    tache.setSize(tache.getCouplesTextes().size());

                    // Sauvegarder la tâche avec les couples
                    tacheRepository.save(tache);

                    System.out.println("Tache mise à jour pour annotateur " + annotateur.getId() + " : size = " + tache.getSize());
                    for (CoupleTexte ct : tache.getCouplesTextes()) {
                        System.out.println("  - Couple assigné : Texte1: " + ct.getTexte1() + ", Texte2: " + ct.getTexte2() + ", tache_id: " + ct.getTache().getId());
                    }
                }
            }
        }
    }
    @Transactional(readOnly = true)
    public DataSet findDatasetById(Long id) {
        return getAllDatasets().stream()
                .filter(d -> d.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Map<String, List<Annotateur>> getAnnotatorsForAssignment(Long datasetId) {
        DataSet dataset = findDatasetById(datasetId);
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset non trouvé avec l'ID : " + datasetId);
        }

        List<Annotateur> allAnnotators = annotateurRepository.findAll();

        List<Annotateur> assignedAnnotators = dataset.getTaches().stream()
                .map(Tache::getAnnotateur)
                .distinct()
                .collect(Collectors.toList());

        List<Annotateur> availableAnnotators = allAnnotators.stream()
                .filter(annotator -> !assignedAnnotators.contains(annotator))
                .collect(Collectors.toList());

        Map<String, List<Annotateur>> result = new HashMap<>();
        result.put("assigned", assignedAnnotators);
        result.put("available", availableAnnotators);
        return result;
    }

    @Transactional
    public void assignAnnotatorToDataset(Long datasetId, Long annotatorId) {
        DataSet dataset = findDatasetById(datasetId);
        if (dataset == null) {
            throw new IllegalArgumentException("Dataset non trouvé avec l'ID : " + datasetId);
        }

        Annotateur annotator = annotateurRepository.findById(annotatorId)
                .orElse(null);
        if (annotator == null) {
            throw new IllegalArgumentException("Annotateur non trouvé avec l'ID : " + annotatorId);
        }

        boolean isAlreadyAssigned = dataset.getTaches().stream()
                .map(Tache::getAnnotateur)
                .anyMatch(a -> a.getId() == annotatorId);
        if (isAlreadyAssigned) {
            throw new IllegalStateException("L'annotateur est déjà affecté à ce dataset.");
        }

        Tache tache = new Tache();
        tache.setDataset(dataset);
        tache.setAnnotateur(annotator);
        dataset.getTaches().add(tache);
        datasetRepository.save(dataset);
    }
//
//    @Transactional
//    public void removeAnnotatorFromDataset(Long datasetId, Long annotatorId) {
//        DataSet dataset = findDatasetById(datasetId);
//        if (dataset == null) {
//            throw new IllegalArgumentException("Dataset non trouvé avec l'ID : " + datasetId);
//        }
//
//        Tache tacheToRemove = dataset.getTaches().stream()
//                .filter(t -> t.getAnnotateur().getId() == annotatorId)
//                .findFirst()
//                .orElse(null);
//        if (tacheToRemove == null) {
//            throw new IllegalStateException("L'annotateur n'est pas affecté à ce dataset.");
//        }
//
//        dataset.getTaches().remove(tacheToRemove);
//        datasetRepository.save(dataset);
//    }
public void distributeTasksToAnnotators(DataSet dataset, List<Long> annotatorIds) {
    // Récupérer les couples non annotés
    List<CoupleTexte> allCouples = new ArrayList<>(dataset.getCouplesTextes());
    Set<CoupleTexte> annotatedCouples = allCouples.stream()
            .filter(couple -> couple.getAnnotation() != null)
            .collect(Collectors.toSet());
    List<CoupleTexte> unannotatedCouples = allCouples.stream()
            .filter(couple -> !annotatedCouples.contains(couple))
            .collect(Collectors.toList());

    // Mélanger les couples non annotés
    Collections.shuffle(unannotatedCouples, new Random());

    // Calculer la répartition
    int totalUnannotated = unannotatedCouples.size();
    int annotatorCount = annotatorIds.size();
    if (annotatorCount == 0) {
        throw new IllegalArgumentException("Aucun annotateur sélectionné pour la répartition.");
    }
    int couplesPerAnnotator = totalUnannotated / annotatorCount;
    int remainingCouples = totalUnannotated % annotatorCount;

    // Vider la collection existante au lieu de la remplacer
    dataset.getTaches().clear(); // Cela permet à Hibernate de gérer les orphelins correctement

    // Répartir les couples
    int startIndex = 0;
    for (Long annotatorId : annotatorIds) {
        Annotateur annotator = annotateurRepository.findById(annotatorId)
                .orElseThrow(() -> new IllegalArgumentException("Annotateur non trouvé : " + annotatorId));
        int endIndex = startIndex + couplesPerAnnotator + (remainingCouples > 0 ? 1 : 0);
        List<CoupleTexte> taskCouples = unannotatedCouples.subList(startIndex, Math.min(endIndex, unannotatedCouples.size()));
        Tache tache = new Tache();
        tache.setDataset(dataset);
        tache.setAnnotateur(annotator);
        tache.setCouplesTextes(taskCouples);
        dataset.getTaches().add(tache);
        startIndex = endIndex;
        remainingCouples--;
    }
    datasetRepository.save(dataset);
}

    @Transactional
    public void removeAnnotatorFromDataset(Long datasetId, Long annotatorId) {
        DataSet dataset = findDatasetById(datasetId);
        if (dataset != null) {
            // Trouver et supprimer la tâche de l'annotateur sans toucher aux annotations
            Tache taskToRemove = dataset.getTaches().stream()
                    .filter(tache -> tache.getAnnotateur().getId() == annotatorId)
                    .findFirst()
                    .orElse(null);
            if (taskToRemove != null) {
                dataset.getTaches().remove(taskToRemove);
                datasetRepository.save(dataset);
            }
        }
    }

    public List<Annotateur> getAvailableAnnotators(Long datasetId) {
        // Logique pour récupérer les annotateurs non affectés à ce dataset
        DataSet dataset = findDatasetById(datasetId);
        List<Annotateur> allAnnotators = annotateurRepository.findAll();
        if (dataset != null) {
            Set<Long> assignedAnnotatorIds = dataset.getTaches().stream()
                    .map(tache -> tache.getAnnotateur().getId())
                    .collect(Collectors.toSet());
            return allAnnotators.stream()
                    .filter(annotator -> !assignedAnnotatorIds.contains(annotator.getId()))
                    .collect(Collectors.toList());
        }
        return allAnnotators;
    }

    public Annotateur findAnnotatorById(Long id) {
        return annotateurRepository.findById(id).orElse(null);
    }


}
