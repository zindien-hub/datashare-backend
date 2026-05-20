package com.datashare.dto.file;

import java.time.LocalDateTime;

// Réponse renvoyée après un upload réussi.
public record FileUploadResponse(
        Long id,
        String originalName,
        Long size,
        String downloadToken,
        String downloadUrl,
        LocalDateTime expiresAt
) {
}