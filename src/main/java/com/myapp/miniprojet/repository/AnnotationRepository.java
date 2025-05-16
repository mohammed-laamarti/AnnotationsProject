package com.myapp.miniprojet.repository;

import com.myapp.miniprojet.model.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnotationRepository extends JpaRepository<Annotation,Long> {
}
