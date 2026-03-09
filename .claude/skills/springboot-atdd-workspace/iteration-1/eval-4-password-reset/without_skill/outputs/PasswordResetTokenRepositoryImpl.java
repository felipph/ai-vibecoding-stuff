package com.example.auth.infrastructure.persistence.repository;

import com.example.auth.domain.model.PasswordResetToken;
import com.example.auth.domain.repository.PasswordResetTokenRepository;
import com.example.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Implementation of PasswordResetTokenRepository using JPA.
 */
@Repository
public class PasswordResetTokenRepositoryImpl implements PasswordResetTokenRepository {

    private final PasswordResetTokenJpaRepository jpaRepository;

    public PasswordResetTokenRepositoryImpl(PasswordResetTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<PasswordResetToken> findByToken(String token) {
        return jpaRepository.findByToken(token)
            .map(this::toDomain);
    }

    @Override
    public PasswordResetToken save(PasswordResetToken token) {
        PasswordResetTokenEntity entity = toEntity(token);
        PasswordResetTokenEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void markAsUsed(String token) {
        jpaRepository.markAsUsed(token);
    }

    @Override
    public void invalidateAllTokensForUser(Long userId) {
        jpaRepository.invalidateAllTokensForUser(userId);
    }

    @Override
    public int deleteExpiredTokens() {
        return jpaRepository.deleteExpiredTokens(Instant.now());
    }

    /**
     * Converts domain model to JPA entity.
     */
    private PasswordResetTokenEntity toEntity(PasswordResetToken token) {
        PasswordResetTokenEntity entity = new PasswordResetTokenEntity(
            token.getUserId(),
            token.getToken(),
            token.getExpiresAt()
        );
        if (token.isUsed()) {
            entity.markAsUsed();
        }
        return entity;
    }

    /**
     * Converts JPA entity to domain model.
     */
    private PasswordResetToken toDomain(PasswordResetTokenEntity entity) {
        return new PasswordResetToken(
            entity.getUserId(),
            entity.getToken(),
            entity.getExpiresAt(),
            entity.isUsed(),
            entity.getCreatedAt()
        );
    }
}
