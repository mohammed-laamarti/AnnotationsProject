package com.myapp.miniprojet.repository;

import com.myapp.miniprojet.model.DataSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataSetRepository extends JpaRepository<DataSet,Long> {
    boolean existsByNom(String nom);
}
