package com.example.auth.infrastructure.persistence.repository;

import com.example.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA Repository for PasswordResetTokenEntity.
 */
@Repository
public interface PasswordResetTokenJpaRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    /**
     * Finds a token by its value.
     */
    Optional<PasswordResetTokenEntity> findByToken(String token);

    /**
     * Marks a token as used.
     */
    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.used = true WHERE t.token = :token")
    void markAsUsed(@Param("token") String token);

    /**
     * Marks all tokens for a user as used.
     */
    @Modifying
    @Query("UPDATE PasswordResetTokenEntity t SET t.used = true WHERE t.userId = :userId AND t.used = false")
    int invalidateAllTokensForUser(@Param("userId") Long userId);

    /**
     * Deletes expired tokens.
     */
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.expiresAt < :now")
    int deleteExpiredTokens(@Param("now") java.time.Instant now);
}
