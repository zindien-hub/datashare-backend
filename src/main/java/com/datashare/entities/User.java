package com.datashare.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Email unique utilisé pour l'inscription et la connexion.
    @Column(nullable = false, unique = true)
    private String email;

    // Mot de passe stocké hashé, jamais en clair.
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // Date de création du compte.
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Date de dernière mise à jour du compte.
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Initialise automatiquement les horodatages lors de la création.
    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // Met à jour automatiquement updatedAt à chaque modification.
    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
