package com.datashare.service;

import com.datashare.dto.file.FileUploadResponse;
import com.datashare.dto.file.FileListItemResponse;
import com.datashare.entities.SharedFile;
import com.datashare.entities.User;
import com.datashare.repository.SharedFileRepository;
import com.datashare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;


@Service
@RequiredArgsConstructor
public class FileService {

    private final SharedFileRepository sharedFileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    // Gère l'upload du fichier, son stockage local et la persistance des métadonnées.
    public FileUploadResponse upload(MultipartFile file, Authentication authentication) throws IOException {
        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String storedName = UUID.randomUUID() + "_" + originalName;
        String downloadToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        fileStorageService.store(file, storedName);

        SharedFile sharedFile = new SharedFile();
        sharedFile.setOriginalName(originalName);
        sharedFile.setStoredName(storedName);
        sharedFile.setContentType(file.getContentType());
        sharedFile.setSize(file.getSize());
        sharedFile.setDownloadToken(downloadToken);
        sharedFile.setExpiresAt(expiresAt);
        sharedFile.setOwner(owner);

        SharedFile saved = sharedFileRepository.save(sharedFile);

        return new FileUploadResponse(
                saved.getId(),
                saved.getOriginalName(),
                saved.getDownloadToken(),
                "/download/" + saved.getDownloadToken(),
                saved.getExpiresAt()
        );
    }

    // Retourne l'historique des fichiers de l'utilisateur connecté.
    public List<FileListItemResponse> getUserFiles(Authentication authentication) {
        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));

        return sharedFileRepository.findByOwnerOrderByCreatedAtDesc(owner)
                .stream()
                .map(file -> new FileListItemResponse(
                        file.getId(),
                        file.getOriginalName(),
                        file.getContentType(),
                        file.getSize(),
                        file.getDownloadToken(),
                        "/download/" + file.getDownloadToken(),
                        file.getExpiresAt(),
                        file.getCreatedAt()))
                .toList();
    }
}
