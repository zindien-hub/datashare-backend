package com.datashare.dto.file;

import java.time.LocalDateTime;

// Représente un fichier dans la liste d'historique utilisateur.
public record FileListItemResponse(
        Long id,
        String originalName,
        String contentType,
        Long size,
        String downloadToken,
        String downloadUrl,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
}
