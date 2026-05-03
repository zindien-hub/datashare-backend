package com.datashare.repository;

import com.datashare.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Recherche un utilisateur par son email.
    Optional<User> findByEmail(String email);

    // Vérifie rapidement si un email existe déjà en base.
    boolean existsByEmail(String email);
}
