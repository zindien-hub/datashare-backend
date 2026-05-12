package com.datashare.controller;

import com.datashare.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/download")
@RequiredArgsConstructor
@Tag(name = "Public Download", description = "Endpoint public de téléchargement")
public class PublicFileController {

    private final FileService fileService;

    // Endpoint public de téléchargement d'un fichier via son token.
    @Operation(summary = "Download a file by token", description = "Télécharge publiquement un fichier via son token")
    @ApiResponse(responseCode = "200", description = "Téléchargement du fichier")
    @ApiResponse(responseCode = "400", description = "Token invalide ou lien expiré")
    @ApiResponse(responseCode = "404", description = "Fichier introuvable")
    @GetMapping("/{token}")
    public ResponseEntity<Resource> download(@PathVariable String token) throws MalformedURLException {
        FileService.FileDownloadData data = fileService.downloadByToken(token);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(data.contentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(data.originalName(), StandardCharsets.UTF_8)
                                .build()
                                .toString()
                )
                .body(data.resource());
    }
}
