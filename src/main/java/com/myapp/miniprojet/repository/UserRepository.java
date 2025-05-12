package com.myapp.miniprojet.repository;

import com.myapp.miniprojet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    User findByLoginAndPassword(String login, String password);
    Optional<User> findByLogin(String login);

    boolean existsByLogin(String admin);
}
