package com.datashare.repository;

import com.datashare.entities.SharedFile;
import com.datashare.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {

    // Recherche un fichier par son token public.
    Optional<SharedFile> findByDownloadToken(String downloadToken);

    // Retourne les fichiers d'un utilisateur, du plus récent au plus ancien.
    List<SharedFile> findByOwnerOrderByCreatedAtDesc(User owner);
}
