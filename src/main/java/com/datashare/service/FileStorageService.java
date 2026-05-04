package com.datashare.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    @Value("${app.file-storage.upload-dir}")
    private String uploadDir;

    private Path uploadPath;

    // Initialise le répertoire de stockage local.
    @PostConstruct
    void init() throws IOException {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(this.uploadPath);
    }

    // Sauvegarde physiquement le fichier sur disque.
    public void store(MultipartFile file, String storedName) throws IOException {
        Path target = uploadPath.resolve(storedName);
        file.transferTo(target);
    }
}
