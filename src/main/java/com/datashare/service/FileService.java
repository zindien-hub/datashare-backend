package com.datashare.service;

import com.datashare.configuration.FileUploadProperties;
import com.datashare.dto.file.FileListItemResponse;
import com.datashare.dto.file.FileUploadResponse;
import com.datashare.entities.SharedFile;
import com.datashare.entities.User;
import com.datashare.exception.BadRequestException;
import com.datashare.exception.ForbiddenException;
import com.datashare.exception.NotFoundException;
import com.datashare.repository.SharedFileRepository;
import com.datashare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final SharedFileRepository sharedFileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final FileUploadProperties fileUploadProperties;

    public FileUploadResponse upload(MultipartFile file, Authentication authentication)
            throws IOException {

        String email = authentication.getName();

        validateUpload(file);

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        String sanitizedFilename = sanitizeFilename(file.getOriginalFilename());

        log.info("Upload démarré — user: {}, fichier: '{}', taille: {} octets",
                email, sanitizedFilename, file.getSize());

        String storedName = buildStoredFilename(sanitizedFilename);
        String downloadToken = UUID.randomUUID().toString();
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        fileStorageService.store(file, storedName);

        SharedFile sharedFile = new SharedFile();
        sharedFile.setOriginalName(sanitizedFilename);
        sharedFile.setStoredName(storedName);
        sharedFile.setContentType(file.getContentType());
        sharedFile.setSize(file.getSize());
        sharedFile.setDownloadToken(downloadToken);
        sharedFile.setExpiresAt(expiresAt);
        sharedFile.setOwner(owner);

        SharedFile saved = sharedFileRepository.save(sharedFile);

        log.info("Upload réussi — ID: {}, fichier: '{}', expire: {}",
                saved.getId(), saved.getOriginalName(), saved.getExpiresAt());

        return new FileUploadResponse(
                saved.getId(),
                saved.getOriginalName(),
                saved.getSize(),
                saved.getDownloadToken(),
                "/download/" + saved.getDownloadToken(),
                saved.getExpiresAt()
        );
    }

    public List<FileListItemResponse> getUserFiles(Authentication authentication) {
        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        List<FileListItemResponse> files = sharedFileRepository
                .findByOwnerOrderByCreatedAtDesc(owner)
                .stream()
                .map(file -> new FileListItemResponse(
                        file.getId(),
                        file.getOriginalName(),
                        file.getContentType(),
                        file.getSize(),
                        file.getDownloadToken(),
                        "/download/" + file.getDownloadToken(),
                        file.getExpiresAt(),
                        file.getCreatedAt()
                ))
                .toList();

        log.info("Liste fichiers — user: {}, {} fichier(s)",
                email, files.size());

        return files;
    }

    public static record FileDownloadData(
            Resource resource,
            String originalName,
            String contentType) {
    }

    public FileDownloadData downloadByToken(String token) throws MalformedURLException {
        SharedFile file = sharedFileRepository.findByDownloadToken(token)
                .orElseThrow(() -> {
                    log.warn("Download refusé — token inconnu");
                    return new NotFoundException("File not found");
                });

        if (file.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("Download refusé — lien expiré: '{}' (expiré le {})",
                    file.getOriginalName(), file.getExpiresAt());
            throw new BadRequestException("Download link has expired");
        }

        Resource resource = fileStorageService.loadAsResource(file.getStoredName());

        if (!resource.exists() || !resource.isReadable()) {
            log.error("Fichier physique absent — storedName: '{}', ID: {}",
                    file.getStoredName(), file.getId());
            throw new NotFoundException("Stored file is not available");
        }

        String contentType = file.getContentType() != null
                ? file.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        log.info("Download autorisé — fichier: '{}', {} octets",
                file.getOriginalName(), file.getSize());

        return new FileDownloadData(resource, file.getOriginalName(), contentType);
    }

    public void deleteFile(Long fileId, Authentication authentication) throws IOException {
        String email = authentication.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Authenticated user not found"));

        SharedFile file = sharedFileRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found"));

        if (!file.getOwner().getId().equals(owner.getId())) {
            log.warn("SECURITE — Suppression non autorisée : fichier ID={} (proprio: '{}') par '{}'",
                    fileId, file.getOwner().getEmail(), email);
            throw new ForbiddenException("You are not allowed to delete this file");
        }

        fileStorageService.delete(file.getStoredName());
        sharedFileRepository.delete(file);

        log.info("Fichier supprimé — ID: {}, fichier: '{}', par: {}",
                fileId, file.getOriginalName(), email);
    }

    public void deleteFiles(List<Long> fileIds, Authentication authentication) throws IOException {
        if (fileIds == null || fileIds.isEmpty()) {
            throw new BadRequestException("No files selected");
        }

        if (fileIds.stream().anyMatch(id -> id == null)) {
            throw new BadRequestException("Invalid file selection");
        }

        log.info("Suppression multiple demandée — {} fichier(s)", fileIds.size());

        for (Long fileId : fileIds) {
            deleteFile(fileId, authentication);
        }
    }

    private void validateUpload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Upload refusé — fichier vide");
            throw new BadRequestException("Uploaded file is empty");
        }

        if (file.getSize() > fileUploadProperties.getMaxSizeBytes()) {
            log.warn("Upload refusé — taille {} octets dépasse la limite {} octets",
                    file.getSize(),
                    fileUploadProperties.getMaxSizeBytes());
            throw new BadRequestException("File size exceeds the allowed limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !fileUploadProperties.getAllowedContentTypes().contains(contentType)) {
            log.warn("Upload refusé — type '{}' non autorisé. Types acceptés : {}",
                    contentType,
                    fileUploadProperties.getAllowedContentTypes());
            throw new BadRequestException("File type is not allowed");
        }
    }

    private String sanitizeFilename(String originalFilename) {
        String cleaned = StringUtils.cleanPath(
                originalFilename == null ? "" : originalFilename
        ).trim();

        if (cleaned.isBlank()) {
            throw new BadRequestException("Filename is invalid");
        }
        if (cleaned.contains("..")) {
            throw new BadRequestException("Filename contains invalid path sequence");
        }

        String filenameOnly = Paths.get(cleaned).getFileName().toString();
        String safeName = filenameOnly.replaceAll("[^a-zA-Z0-9._\\-]", "_");

        if (safeName.isBlank() || safeName.equals("_")) {
            throw new BadRequestException("Filename is invalid after sanitization");
        }

        return safeName;
    }

    private String buildStoredFilename(String sanitizedFilename) {
        return UUID.randomUUID() + "_" + sanitizedFilename;
    }
}