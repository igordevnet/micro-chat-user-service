package com.microservice.microchatuserservice.infrastructure.persistence.jobs;

import com.microservice.microchatuserservice.infrastructure.persistence.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupJob {

    private final TokenRepository repository;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanExpiredTokens() {
        log.info("Starting cleanup of expired and revoked tokens...");
        try {
            repository.deleteTokensWhereExpiredOrRevoked();
            log.info("Token cleanup completed successfully.");
        } catch (Exception e) {
            log.error("Failed to perform token cleanup: {}", e.getMessage());
        }
    }
}