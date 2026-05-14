package com.datashare.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@Service
public class FileStorageService {

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    private Path uploadPath;

    @PostConstruct
    void init() throws IOException {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadPath);
        log.info("Répertoire de stockage initialisé : {}", this.uploadPath);
    }

    public void store(MultipartFile file, String storedName) throws IOException {
        Path target = resolveSecure(storedName);
        file.transferTo(target);
        log.info("Fichier stocké : '{}', taille : {} octets", storedName, file.getSize());
    }

    public Resource loadAsResource(String storedName) throws MalformedURLException {
        Path filePath = resolveSecure(storedName);
        log.debug("Chargement ressource : '{}'", storedName);
        return new UrlResource(filePath.toUri());
    }

    public void delete(String storedName) throws IOException {
        Path target = resolveSecure(storedName);
        boolean deleted = Files.deleteIfExists(target);

        if (deleted) {
            log.info("Fichier supprimé physiquement : '{}'", storedName);
        } else {
            log.warn("Fichier physique introuvable lors de la suppression : '{}'", storedName);
        }
    }

    private Path resolveSecure(String storedName) {
        Path resolved = uploadPath.resolve(storedName).normalize();

        if (!resolved.startsWith(uploadPath)) {
            log.error("SECURITE — Accès hors répertoire détecté : storedName='{}'", storedName);
            throw new IllegalArgumentException("Nom de fichier non autorisé : " + storedName);
        }

        return resolved;
    }
}
