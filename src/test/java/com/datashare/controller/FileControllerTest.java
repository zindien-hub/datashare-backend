package com.datashare.controller;

import com.datashare.dto.file.BulkDeleteRequest;
import com.datashare.dto.file.FileListItemResponse;
import com.datashare.dto.file.FileUploadResponse;
import com.datashare.exception.BadRequestException;
import com.datashare.service.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class FileControllerTest {

    private final FileService fileService = mock(FileService.class);
    private final FileController fileController = new FileController(fileService);

    private final Authentication authentication = mock(Authentication.class);
    private final MultipartFile multipartFile = mock(MultipartFile.class);

    @Test
    void shouldUploadFile() throws Exception {
        FileUploadResponse uploadResponse = new FileUploadResponse(
                1L,
                "document.txt",
                120L,
                "token-123",
                "/download/token-123",
                LocalDateTime.now().plusDays(7)
        );

        when(fileService.upload(multipartFile, authentication)).thenReturn(uploadResponse);

        ResponseEntity<FileUploadResponse> response = fileController.upload(multipartFile, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("document.txt", response.getBody().originalName());
        assertEquals(120L, response.getBody().size());
        assertEquals("token-123", response.getBody().downloadToken());

        verify(fileService).upload(multipartFile, authentication);
    }

    @Test
    void shouldReturnFileHistory() {
        List<FileListItemResponse> files = List.of(
                new FileListItemResponse(
                        1L,
                        "document.txt",
                        "text/plain",
                        120L,
                        "token-123",
                        "/download/token-123",
                        LocalDateTime.now().plusDays(7),
                        LocalDateTime.now()
                )
        );

        when(fileService.getUserFiles(authentication)).thenReturn(files);

        ResponseEntity<List<FileListItemResponse>> response = fileController.getMyFiles(authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("document.txt", response.getBody().get(0).originalName());

        verify(fileService).getUserFiles(authentication);
    }

    @Test
    void shouldDeleteFile() throws Exception {
        ResponseEntity<Void> response = fileController.deleteFile(1L, authentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(fileService).deleteFile(1L, authentication);
    }

    @Test
    void shouldDeleteMultipleFiles() throws Exception {
        BulkDeleteRequest request = new BulkDeleteRequest(List.of(1L, 2L, 3L));

        ResponseEntity<Void> response = fileController.deleteFiles(request, authentication);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());

        verify(fileService).deleteFiles(List.of(1L, 2L, 3L), authentication);
    }

    @Test
    void shouldRejectBulkDeleteWhenRequestBodyIsNull() throws Exception {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileController.deleteFiles(null, authentication));

        assertEquals("No files selected", exception.getMessage());
        verify(fileService, never()).deleteFiles(anyList(), eq(authentication));
    }
}