package com.myapp.miniprojet.repository;

import com.myapp.miniprojet.model.Tache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TacheRepository extends JpaRepository<Tache,Long> {
    List<Tache> findByDatasetId(Long datasetId);
    Optional<Tache> findByDatasetIdAndAnnotateurId(Long datasetId, Long annotateurId);
}
