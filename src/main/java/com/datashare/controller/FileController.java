package com.datashare.controller;

import com.datashare.dto.file.FileUploadResponse;
import com.datashare.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // Endpoint d'upload de fichier pour un utilisateur authentifié.
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<FileUploadResponse> upload(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        FileUploadResponse response = fileService.upload(file, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
