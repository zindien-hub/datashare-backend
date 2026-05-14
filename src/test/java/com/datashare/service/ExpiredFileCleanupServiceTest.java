package com.datashare.service;

import com.datashare.entities.SharedFile;
import com.datashare.repository.SharedFileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpiredFileCleanupServiceTest {

    @Mock
    private SharedFileRepository sharedFileRepository;

    @Mock
    private FileStorageService fileStorageService;

    private ExpiredFileCleanupService expiredFileCleanupService;

    @BeforeEach
    void setUp() {
        expiredFileCleanupService = new ExpiredFileCleanupService(
                sharedFileRepository,
                fileStorageService
        );
    }

    @Test
    void shouldDeleteExpiredFiles() throws Exception {
        SharedFile file1 = buildExpiredFile(1L, "doc1.txt", "uuid_doc1.txt");
        SharedFile file2 = buildExpiredFile(2L, "doc2.txt", "uuid_doc2.txt");

        when(sharedFileRepository.findByExpiresAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(file1, file2));

        int deleted = expiredFileCleanupService.purgeExpiredFiles();

        assertEquals(2, deleted);
        verify(fileStorageService).delete("uuid_doc1.txt");
        verify(fileStorageService).delete("uuid_doc2.txt");
        verify(sharedFileRepository).delete(file1);
        verify(sharedFileRepository).delete(file2);
    }

    @Test
    void shouldReturnZeroWhenNoExpiredFiles() {
        when(sharedFileRepository.findByExpiresAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());

        int deleted = expiredFileCleanupService.purgeExpiredFiles();

        assertEquals(0, deleted);
        verifyNoInteractions(fileStorageService);
        verify(sharedFileRepository, never()).delete(any());
    }

    @Test
    void shouldKeepDatabaseRowWhenPhysicalDeleteFails() throws Exception {
        SharedFile file = buildExpiredFile(1L, "doc1.txt", "uuid_doc1.txt");

        when(sharedFileRepository.findByExpiresAtBefore(any(LocalDateTime.class)))
                .thenReturn(List.of(file));

        doThrow(new IOException("disk error"))
                .when(fileStorageService).delete("uuid_doc1.txt");

        int deleted = expiredFileCleanupService.purgeExpiredFiles();

        assertEquals(0, deleted);
        verify(fileStorageService).delete("uuid_doc1.txt");
        verify(sharedFileRepository, never()).delete(any());
    }

    private SharedFile buildExpiredFile(Long id, String originalName, String storedName) {
        SharedFile file = new SharedFile();
        file.setId(id);
        file.setOriginalName(originalName);
        file.setStoredName(storedName);
        file.setExpiresAt(LocalDateTime.now().minusDays(1));
        return file;
    }
}
