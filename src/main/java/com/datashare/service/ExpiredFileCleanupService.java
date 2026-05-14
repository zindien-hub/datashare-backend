package com.datashare.service;

import com.datashare.entities.SharedFile;
import com.datashare.repository.SharedFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpiredFileCleanupService {

    private final SharedFileRepository sharedFileRepository;
    private final FileStorageService fileStorageService;

    public int purgeExpiredFiles() {
        LocalDateTime now = LocalDateTime.now();
        List<SharedFile> expiredFiles = sharedFileRepository.findByExpiresAtBefore(now);

        if (expiredFiles.isEmpty()) {
            log.info("Purge expirations — aucun fichier expiré à supprimer");
            return 0;
        }

        int deletedCount = 0;

        for (SharedFile file : expiredFiles) {
            try {
                fileStorageService.delete(file.getStoredName());
                sharedFileRepository.delete(file);
                deletedCount++;

                log.info(
                        "Purge expirations — fichier supprimé : id={}, originalName='{}', expiresAt={}",
                        file.getId(),
                        file.getOriginalName(),
                        file.getExpiresAt()
                );
            } catch (IOException exception) {
                log.error(
                        "Purge expirations — échec suppression physique : id={}, storedName='{}'",
                        file.getId(),
                        file.getStoredName(),
                        exception
                );
            } catch (Exception exception) {
                log.error(
                        "Purge expirations — échec suppression logique : id={}, originalName='{}'",
                        file.getId(),
                        file.getOriginalName(),
                        exception
                );
            }
        }

        log.info(
                "Purge expirations — {} fichier(s) expiré(s) supprimé(s) sur {} détecté(s)",
                deletedCount,
                expiredFiles.size()
        );

        return deletedCount;
    }
}
