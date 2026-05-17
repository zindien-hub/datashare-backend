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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private SharedFileRepository sharedFileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private Authentication authentication;

    @Mock
    private MultipartFile multipartFile;

    private FileUploadProperties fileUploadProperties;

    private FileService fileService;

    @BeforeEach
    void setUp() {
        fileUploadProperties = new FileUploadProperties();
        fileUploadProperties.setMaxSizeBytes(5_242_880L);
        fileUploadProperties.setAllowedContentTypes(List.of(
                "image/png",
                "image/jpeg",
                "application/pdf",
                "text/plain"
        ));

        fileService = new FileService(
                sharedFileRepository,
                userRepository,
                fileStorageService,
                fileUploadProperties
        );
    }

    @Test
    void shouldUploadValidFile() throws Exception {
        User user = buildUser(1L, "test@datashare.com");

        when(authentication.getName()).thenReturn("test@datashare.com");
        when(userRepository.findByEmail("test@datashare.com")).thenReturn(Optional.of(user));

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getOriginalFilename()).thenReturn("document.txt");

        when(sharedFileRepository.save(any(SharedFile.class))).thenAnswer(invocation -> {
            SharedFile file = invocation.getArgument(0);
            file.setId(10L);
            return file;
        });

        FileUploadResponse response = fileService.upload(multipartFile, authentication);

        assertNotNull(response);
        assertEquals(10L, response.id());
        assertEquals("document.txt", response.originalName());
        assertNotNull(response.downloadToken());
        assertTrue(response.downloadUrl().startsWith("/download/"));
        assertNotNull(response.expiresAt());

        verify(fileStorageService).store(eq(multipartFile), anyString());

        ArgumentCaptor<SharedFile> captor = ArgumentCaptor.forClass(SharedFile.class);
        verify(sharedFileRepository).save(captor.capture());

        SharedFile savedFile = captor.getValue();
        assertEquals("document.txt", savedFile.getOriginalName());
        assertEquals("text/plain", savedFile.getContentType());
        assertEquals(100L, savedFile.getSize());
        assertEquals(user, savedFile.getOwner());
        assertNotNull(savedFile.getStoredName());
        assertTrue(savedFile.getStoredName().endsWith("_document.txt"));
    }

    @Test
    void shouldRejectEmptyFile() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileService.upload(multipartFile, authentication)
        );

        assertEquals("Uploaded file is empty", exception.getMessage());
        verifyNoInteractions(userRepository, sharedFileRepository, fileStorageService);
    }

    @Test
    void shouldRejectTooLargeFile() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(10_000_000L);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileService.upload(multipartFile, authentication)
        );

        assertEquals("File size exceeds the allowed limit", exception.getMessage());
        verifyNoInteractions(userRepository, sharedFileRepository, fileStorageService);
    }

    @Test
    void shouldRejectUnauthorizedContentType() throws Exception {
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getContentType()).thenReturn("application/x-sh");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileService.upload(multipartFile, authentication)
        );

        assertEquals("File type is not allowed", exception.getMessage());
        verifyNoInteractions(userRepository, sharedFileRepository, fileStorageService);
    }

    @Test
    void shouldRejectFilenameWithPathTraversal() throws Exception {
        User user = buildUser(1L, "test@datashare.com");

        when(authentication.getName()).thenReturn("test@datashare.com");
        when(userRepository.findByEmail("test@datashare.com")).thenReturn(Optional.of(user));

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getOriginalFilename()).thenReturn("../secret.txt");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileService.upload(multipartFile, authentication)
        );

        assertEquals("Filename contains invalid path sequence", exception.getMessage());
        verify(fileStorageService, never()).store(any(), anyString());
        verify(sharedFileRepository, never()).save(any());
    }

    @Test
    void shouldSanitizeFilenameWithSpecialCharacters() throws Exception {
        User user = buildUser(1L, "test@datashare.com");

        when(authentication.getName()).thenReturn("test@datashare.com");
        when(userRepository.findByEmail("test@datashare.com")).thenReturn(Optional.of(user));

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getOriginalFilename()).thenReturn("mon rapport (v2).pdf");

        when(sharedFileRepository.save(any(SharedFile.class))).thenAnswer(invocation -> {
            SharedFile f = invocation.getArgument(0);
            f.setId(1L);
            return f;
        });

        fileService.upload(multipartFile, authentication);

        ArgumentCaptor<SharedFile> captor = ArgumentCaptor.forClass(SharedFile.class);
        verify(sharedFileRepository).save(captor.capture());

        String savedName = captor.getValue().getOriginalName();

        assertFalse(savedName.contains(" "),  "Les espaces doivent être remplacés par _");
        assertFalse(savedName.contains("("),  "Les ( doivent être remplacées par _");
        assertFalse(savedName.contains(")"),  "Les ) doivent être remplacées par _");

        assertEquals("mon_rapport__v2_.pdf", savedName);
        assertTrue(captor.getValue().getStoredName().endsWith("_mon_rapport__v2_.pdf"));
    }

    @Test
    void shouldRejectFilenameReducedToSingleUnderscore() throws Exception {
        User user = buildUser(1L, "test@datashare.com");

        when(authentication.getName()).thenReturn("test@datashare.com");
        when(userRepository.findByEmail("test@datashare.com")).thenReturn(Optional.of(user));

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getOriginalFilename()).thenReturn("!");

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileService.upload(multipartFile, authentication)
        );

        assertEquals("Filename is invalid after sanitization", exception.getMessage());
        verify(fileStorageService, never()).store(any(), anyString());
        verify(sharedFileRepository, never()).save(any());
    }

    @Test
    void shouldRejectNullFilename() throws Exception {
        User user = buildUser(1L, "test@datashare.com");

        when(authentication.getName()).thenReturn("test@datashare.com");
        when(userRepository.findByEmail("test@datashare.com")).thenReturn(Optional.of(user));

        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getSize()).thenReturn(100L);
        when(multipartFile.getContentType()).thenReturn("text/plain");
        when(multipartFile.getOriginalFilename()).thenReturn(null);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileService.upload(multipartFile, authentication)
        );

        assertEquals("Filename is invalid", exception.getMessage());
        verify(fileStorageService, never()).store(any(), anyString());
        verify(sharedFileRepository, never()).save(any());
    }

    @Test
    void shouldReturnUserFiles() {
        User user = buildUser(1L, "test@datashare.com");

        SharedFile file1 = buildSharedFile(1L, "a.txt", "text/plain", 10L, "token-a");
        file1.setOwner(user);
        file1.setCreatedAt(LocalDateTime.now());

        SharedFile file2 = buildSharedFile(2L, "b.pdf", "application/pdf", 20L, "token-b");
        file2.setOwner(user);
        file2.setCreatedAt(LocalDateTime.now().minusHours(1));

        when(authentication.getName()).thenReturn("test@datashare.com");
        when(userRepository.findByEmail("test@datashare.com")).thenReturn(Optional.of(user));
        when(sharedFileRepository.findByOwnerOrderByCreatedAtDesc(user))
                .thenReturn(List.of(file1, file2));

        List<FileListItemResponse> result = fileService.getUserFiles(authentication);

        assertEquals(2, result.size());
        assertEquals("a.txt", result.get(0).originalName());
        assertEquals("/download/token-a", result.get(0).downloadUrl());
        assertEquals("b.pdf", result.get(1).originalName());
    }

    @Test
    void shouldDeleteOwnedFile() throws Exception {
        User user = buildUser(1L, "test@datashare.com");

        SharedFile file = buildSharedFile(5L, "doc.txt", "text/plain", 12L, "token-1");
        file.setStoredName("uuid_doc.txt");
        file.setOwner(user);

        when(authentication.getName()).thenReturn("test@datashare.com");
        when(userRepository.findByEmail("test@datashare.com")).thenReturn(Optional.of(user));
        when(sharedFileRepository.findById(5L)).thenReturn(Optional.of(file));

        fileService.deleteFile(5L, authentication);

        verify(fileStorageService).delete("uuid_doc.txt");
        verify(sharedFileRepository).delete(file);
    }

    @Test
    void shouldRejectDeleteForAnotherOwner() throws Exception {
        User owner = buildUser(1L, "owner@datashare.com");
        User otherUser = buildUser(2L, "other@datashare.com");

        SharedFile file = buildSharedFile(5L, "doc.txt", "text/plain", 12L, "token-1");
        file.setOwner(owner);

        when(authentication.getName()).thenReturn("other@datashare.com");
        when(userRepository.findByEmail("other@datashare.com")).thenReturn(Optional.of(otherUser));
        when(sharedFileRepository.findById(5L)).thenReturn(Optional.of(file));

        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> fileService.deleteFile(5L, authentication)
        );

        assertEquals("You are not allowed to delete this file", exception.getMessage());
        verify(fileStorageService, never()).delete(anyString());
        verify(sharedFileRepository, never()).delete(any());
    }

    @Test
    void shouldDeleteMultipleOwnedFiles() throws Exception {
        User user = buildUser(1L, "test@datashare.com");

        SharedFile file1 = buildSharedFile(5L, "doc-1.txt", "text/plain", 12L, "token-1");
        file1.setStoredName("uuid_doc_1.txt");
        file1.setOwner(user);

        SharedFile file2 = buildSharedFile(6L, "doc-2.txt", "text/plain", 14L, "token-2");
        file2.setStoredName("uuid_doc_2.txt");
        file2.setOwner(user);

        when(authentication.getName()).thenReturn("test@datashare.com");
        when(userRepository.findByEmail("test@datashare.com")).thenReturn(Optional.of(user));
        when(sharedFileRepository.findById(5L)).thenReturn(Optional.of(file1));
        when(sharedFileRepository.findById(6L)).thenReturn(Optional.of(file2));

        fileService.deleteFiles(List.of(5L, 6L), authentication);

        verify(fileStorageService).delete("uuid_doc_1.txt");
        verify(fileStorageService).delete("uuid_doc_2.txt");
        verify(sharedFileRepository).delete(file1);
        verify(sharedFileRepository).delete(file2);
    }

    @Test
    void shouldRejectDeleteMultipleWhenNoFilesSelected() {
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileService.deleteFiles(List.of(), authentication)
        );

        assertEquals("No files selected", exception.getMessage());
        verifyNoInteractions(userRepository, sharedFileRepository, fileStorageService);
    }

    @Test
    void shouldDownloadFileByToken() throws Exception {
        SharedFile file = buildSharedFile(1L, "doc.txt", "text/plain", 10L, "token-1");
        file.setStoredName("uuid_doc.txt");
        file.setExpiresAt(LocalDateTime.now().plusDays(1));

        ByteArrayResource resource = new ByteArrayResource("hello".getBytes()) {
            @Override
            public String getFilename() {
                return "doc.txt";
            }
        };

        when(sharedFileRepository.findByDownloadToken("token-1")).thenReturn(Optional.of(file));
        when(fileStorageService.loadAsResource("uuid_doc.txt")).thenReturn(resource);

        FileService.FileDownloadData result = fileService.downloadByToken("token-1");

        assertNotNull(result);
        assertEquals("doc.txt", result.originalName());
        assertEquals("text/plain", result.contentType());
        assertNotNull(result.resource());
    }

    @Test
    void shouldRejectExpiredDownloadToken() {
        SharedFile file = buildSharedFile(1L, "doc.txt", "text/plain", 10L, "token-1");
        file.setStoredName("uuid_doc.txt");
        file.setExpiresAt(LocalDateTime.now().minusMinutes(1));

        when(sharedFileRepository.findByDownloadToken("token-1")).thenReturn(Optional.of(file));

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> fileService.downloadByToken("token-1")
        );

        assertEquals("Download link has expired", exception.getMessage());
    }

    @Test
    void shouldThrowNotFoundWhenStoredFileIsMissing() throws Exception {
        SharedFile file = buildSharedFile(1L, "doc.txt", "text/plain", 10L, "token-1");
        file.setStoredName("uuid_doc.txt");
        file.setExpiresAt(LocalDateTime.now().plusDays(1));

        ByteArrayResource unreadableResource = new ByteArrayResource(new byte[0]) {
            @Override
            public boolean exists() {
                return false;
            }

            @Override
            public boolean isReadable() {
                return false;
            }
        };

        when(sharedFileRepository.findByDownloadToken("token-1")).thenReturn(Optional.of(file));
        when(fileStorageService.loadAsResource("uuid_doc.txt")).thenReturn(unreadableResource);

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> fileService.downloadByToken("token-1")
        );

        assertEquals("Stored file is not available", exception.getMessage());
    }

    private User buildUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        return user;
    }

    private SharedFile buildSharedFile(
            Long id, String originalName, String contentType,
            Long size, String token) {

        SharedFile file = new SharedFile();
        file.setId(id);
        file.setOriginalName(originalName);
        file.setContentType(contentType);
        file.setSize(size);
        file.setDownloadToken(token);
        file.setExpiresAt(LocalDateTime.now().plusDays(7));
        file.setCreatedAt(LocalDateTime.now());
        return file;
    }
}