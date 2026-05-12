package com.datashare.controller;

import com.datashare.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class PublicFileControllerTest {

    @Mock
    private FileService fileService;

    private PublicFileController publicFileController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        publicFileController = new PublicFileController(fileService);
    }

    @Test
    void shouldDownloadFileWithExpectedHeaders() throws Exception {
        ByteArrayResource resource = new ByteArrayResource("hello".getBytes(StandardCharsets.UTF_8));

        FileService.FileDownloadData data = new FileService.FileDownloadData(
                resource,
                "document.txt",
                "text/plain"
        );

        when(fileService.downloadByToken("token-123")).thenReturn(data);

        ResponseEntity<org.springframework.core.io.Resource> response =
                publicFileController.download("token-123");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("text/plain", response.getHeaders().getContentType().toString());

        String contentDisposition = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(contentDisposition);
        assertTrue(contentDisposition.contains("attachment"));
        assertTrue(contentDisposition.contains("document.txt"));
    }
}