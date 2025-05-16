package com.myapp.miniprojet.repository;

import com.myapp.miniprojet.model.Annotateur;
import com.myapp.miniprojet.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByLoginAndPassword(String login, String password);
    Optional<User> findByLogin(String login);

    boolean existsByLogin(String admin);
    @Query("SELECT u FROM User u WHERE TYPE(u) = Annotateur")
    List<Annotateur> findAllAnnotateurs();
}
