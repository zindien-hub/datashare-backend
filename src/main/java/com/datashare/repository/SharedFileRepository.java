package com.datashare.repository;

import com.datashare.entities.SharedFile;
import com.datashare.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SharedFileRepository extends JpaRepository<SharedFile, Long> {

    Optional<SharedFile> findByDownloadToken(String downloadToken);

    List<SharedFile> findByOwnerOrderByCreatedAtDesc(User owner);
    
    List<SharedFile> findByExpiresAtBefore(LocalDateTime now);
}
