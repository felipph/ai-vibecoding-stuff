package com.example.auth.infrastructure.persistence.repository;

import com.example.auth.domain.model.PasswordResetToken;
import com.example.auth.infrastructure.persistence.entity.PasswordResetTokenEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PasswordResetTokenRepositoryImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Password Reset Token Repository Implementation")
class PasswordResetTokenRepositoryImplTest {

    @Mock
    private PasswordResetTokenJpaRepository jpaRepository;

    @InjectMocks
    private PasswordResetTokenRepositoryImpl repository;

    @Nested
    @DisplayName("Find By Token")
    class FindByToken {

        @Test
        @DisplayName("should return token when found")
        void shouldReturnTokenWhenFound() {
            // Arrange
            String tokenValue = "test-token-123";
            PasswordResetTokenEntity entity = new PasswordResetTokenEntity(
                1L,
                tokenValue,
                Instant.now().plus(1, ChronoUnit.HOURS)
            );

            when(jpaRepository.findByToken(tokenValue)).thenReturn(Optional.of(entity));

            // Act
            Optional<PasswordResetToken> result = repository.findByToken(tokenValue);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().getToken()).isEqualTo(tokenValue);
            assertThat(result.get().getUserId()).isEqualTo(1L);
            assertThat(result.get().isUsed()).isFalse();
        }

        @Test
        @DisplayName("should return empty when token not found")
        void shouldReturnEmptyWhenTokenNotFound() {
            // Arrange
            String tokenValue = "nonexistent-token";
            when(jpaRepository.findByToken(tokenValue)).thenReturn(Optional.empty());

            // Act
            Optional<PasswordResetToken> result = repository.findByToken(tokenValue);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should map used status correctly")
        void shouldMapUsedStatusCorrectly() {
            // Arrange
            String tokenValue = "used-token-123";
            PasswordResetTokenEntity entity = new PasswordResetTokenEntity(
                1L,
                tokenValue,
                Instant.now().plus(1, ChronoUnit.HOURS)
            );
            entity.markAsUsed();

            when(jpaRepository.findByToken(tokenValue)).thenReturn(Optional.of(entity));

            // Act
            Optional<PasswordResetToken> result = repository.findByToken(tokenValue);

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().isUsed()).isTrue();
        }
    }

    @Nested
    @DisplayName("Save Token")
    class SaveToken {

        @Test
        @DisplayName("should save token and return with generated id")
        void shouldSaveTokenAndReturnWithGeneratedId() {
            // Arrange
            PasswordResetToken token = new PasswordResetToken(
                1L,
                "test-token-123",
                Instant.now().plus(1, ChronoUnit.HOURS)
            );

            PasswordResetTokenEntity savedEntity = new PasswordResetTokenEntity(
                1L,
                "test-token-123",
                Instant.now().plus(1, ChronoUnit.HOURS)
            );

            when(jpaRepository.save(any(PasswordResetTokenEntity.class))).thenReturn(savedEntity);

            // Act
            PasswordResetToken result = repository.save(token);

            // Assert
            assertThat(result.getToken()).isEqualTo("test-token-123");
            assertThat(result.getUserId()).isEqualTo(1L);
            verify(jpaRepository).save(any(PasswordResetTokenEntity.class));
        }
    }

    @Nested
    @DisplayName("Mark As Used")
    class MarkAsUsed {

        @Test
        @DisplayName("should mark token as used")
        void shouldMarkTokenAsUsed() {
            // Arrange
            String tokenValue = "test-token-123";

            // Act
            repository.markAsUsed(tokenValue);

            // Assert
            verify(jpaRepository).markAsUsed(tokenValue);
        }
    }

    @Nested
    @DisplayName("Invalidate All Tokens For User")
    class InvalidateAllTokensForUser {

        @Test
        @DisplayName("should invalidate all tokens for user")
        void shouldInvalidateAllTokensForUser() {
            // Arrange
            Long userId = 1L;
            when(jpaRepository.invalidateAllTokensForUser(userId)).thenReturn(3);

            // Act
            repository.invalidateAllTokensForUser(userId);

            // Assert
            verify(jpaRepository).invalidateAllTokensForUser(userId);
        }
    }

    @Nested
    @DisplayName("Delete Expired Tokens")
    class DeleteExpiredTokens {

        @Test
        @DisplayName("should delete expired tokens and return count")
        void shouldDeleteExpiredTokensAndReturnCount() {
            // Arrange
            when(jpaRepository.deleteExpiredTokens(any(Instant.class))).thenReturn(5);

            // Act
            int count = repository.deleteExpiredTokens();

            // Assert
            assertThat(count).isEqualTo(5);
            verify(jpaRepository).deleteExpiredTokens(any(Instant.class));
        }
    }
}
