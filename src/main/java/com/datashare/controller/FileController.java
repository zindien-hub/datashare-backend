package com.datashare.controller;

import com.datashare.dto.file.FileUploadResponse;
import com.datashare.dto.file.FileListItemResponse;
import com.datashare.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "Endpoints protégés de gestion des fichiers")
@SecurityRequirement(name = "bearerAuth")
public class FileController {

    private final FileService fileService;

    // Endpoint d'upload de fichier pour un utilisateur authentifié.
    @Operation(summary = "Upload a file", description = "Téléverse un fichier pour l'utilisateur authentifié")
    @ApiResponse(responseCode = "201", description = "Fichier téléversé avec succès")
    @ApiResponse(responseCode = "400", description = "Requête invalide")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {
        FileUploadResponse response = fileService.upload(file, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Retourne l'historique des fichiers de l'utilisateur authentifié.
    @Operation(summary = "Get current user's file history", description = "Retourne l'historique des fichiers de l'utilisateur authentifié")
    @ApiResponse(responseCode = "200", description = "Historique récupéré avec succès")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    @GetMapping
    public ResponseEntity<List<FileListItemResponse>> getMyFiles(Authentication authentication) {
        List<FileListItemResponse> response = fileService.getUserFiles(authentication);
        return ResponseEntity.ok(response);
    }

    // Supprime un fichier appartenant à l'utilisateur authentifié.
    @Operation(summary = "Delete a file", description = "Supprime un fichier appartenant à l'utilisateur authentifié")
    @ApiResponse(responseCode = "204", description = "Fichier supprimé avec succès")
    @ApiResponse(responseCode = "401", description = "Non authentifié")
    @ApiResponse(responseCode = "403", description = "Suppression interdite")
    @ApiResponse(responseCode = "404", description = "Fichier introuvable")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(
            @PathVariable Long id,
            Authentication authentication) throws IOException {
        fileService.deleteFile(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
