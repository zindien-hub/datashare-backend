package com.datashare.controller;

import com.datashare.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/download")
@RequiredArgsConstructor
public class PublicFileController {

    private final FileService fileService;

    // Endpoint public de téléchargement d'un fichier via son token.
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
