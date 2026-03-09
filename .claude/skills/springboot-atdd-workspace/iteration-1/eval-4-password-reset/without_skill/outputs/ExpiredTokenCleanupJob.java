package com.example.auth.infrastructure.scheduler;

import com.example.auth.domain.repository.PasswordResetTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled job to clean up expired password reset tokens.
 * Runs every hour by default.
 */
@Component
@ConditionalOnProperty(
    prefix = "password.reset.token.cleanup",
    name = "enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class ExpiredTokenCleanupJob {

    private static final Logger logger = LoggerFactory.getLogger(ExpiredTokenCleanupJob.class);

    private final PasswordResetTokenRepository tokenRepository;

    public ExpiredTokenCleanupJob(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Deletes expired tokens from the database.
     * Runs every hour by default (configurable via cron expression).
     */
    @Scheduled(cron = "${password.reset.token.cleanup.cron:0 0 * * * *}")
    public void cleanupExpiredTokens() {
        logger.info("Starting expired token cleanup job");

        try {
            int deletedCount = tokenRepository.deleteExpiredTokens();
            logger.info("Expired token cleanup completed. Deleted {} tokens", deletedCount);
        } catch (Exception e) {
            logger.error("Error during expired token cleanup", e);
        }
    }
}
