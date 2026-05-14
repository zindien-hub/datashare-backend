package com.datashare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExpiredFileCleanupScheduler {

    private final ExpiredFileCleanupService expiredFileCleanupService;

    @Scheduled(cron = "${app.cleanup.expired-files-cron:0 0 * * * *}")
    public void purgeExpiredFiles() {
        log.info("Début du job planifié de purge des fichiers expirés");
        int deleted = expiredFileCleanupService.purgeExpiredFiles();
        log.info("Fin du job planifié de purge des fichiers expirés — {} fichier(s) supprimé(s)", deleted);
    }
}
