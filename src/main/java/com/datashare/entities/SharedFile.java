package com.datashare.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "shared_files")
@Getter
@Setter
@NoArgsConstructor
public class SharedFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom du fichier tel qu'il a été envoyé par l'utilisateur.
    @Column(name = "original_name", nullable = false)
    private String originalName;

    // Nom du fichier sur le disque pour éviter les collisions.
    @Column(name = "stored_name", nullable = false, unique = true)
    private String storedName;

    // Type MIME du fichier.
    @Column(name = "content_type")
    private String contentType;

    // Taille du fichier en octets.
    @Column(nullable = false)
    private Long size;

    // Token unique utilisé dans le lien de téléchargement.
    @Column(name = "download_token", nullable = false, unique = true)
    private String downloadToken;

    // Date d'expiration du lien.
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    // Mot de passe optionnel pour protéger le téléchargement.
    @Column(name = "password_hash")
    private String passwordHash;

    // Utilisateur propriétaire du fichier.
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
