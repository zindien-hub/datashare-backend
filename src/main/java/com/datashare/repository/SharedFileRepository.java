package com.datashare.repository;

import com.datashare.entities.SharedFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {

    // Recherche un fichier par son token public.
    Optional<SharedFile> findByDownloadToken(String downloadToken);
}
