package com.myapp.miniprojet.repository;

import com.myapp.miniprojet.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Integer> {
    Role findByRole(String admin);
}
