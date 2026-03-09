package com.example.auth.repository;

import com.example.auth.model.PasswordResetToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ATDD Repository tests for PasswordResetToken entity.
 * Uses Testcontainers for real database testing.
 */
@DataJpaTest
@Testcontainers
class PasswordResetTokenRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Nested
    @DisplayName("findByToken")
    class FindByToken {

        @Test
        @DisplayName("with existing token - returns token")
        void withExistingToken_returnsToken() {
            // Arrange
            PasswordResetToken token = PasswordResetToken.builder()
                    .token("abc123")
                    .email("user@example.com")
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();
            tokenRepository.save(token);

            // Act
            Optional<PasswordResetToken> found = tokenRepository.findByToken("abc123");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getToken()).isEqualTo("abc123");
            assertThat(found.get().getEmail()).isEqualTo("user@example.com");
            assertThat(found.get().isUsed()).isFalse();
        }

        @Test
        @DisplayName("with non-existent token - returns empty")
        void withNonExistentToken_returnsEmpty() {
            // Act
            Optional<PasswordResetToken> found = tokenRepository.findByToken("notfound");

            // Assert
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByEmail")
    class DeleteByEmail {

        @Test
        @DisplayName("deletes all tokens for given email")
        void deletesAllTokensForGivenEmail() {
            // Arrange
            String email = "user@example.com";

            PasswordResetToken token1 = PasswordResetToken.builder()
                    .token("token1")
                    .email(email)
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();

            PasswordResetToken token2 = PasswordResetToken.builder()
                    .token("token2")
                    .email(email)
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();

            PasswordResetToken otherToken = PasswordResetToken.builder()
                    .token("token3")
                    .email("other@example.com")
                    .expiryDate(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();

            tokenRepository.save(token1);
            tokenRepository.save(token2);
            tokenRepository.save(otherToken);

            // Act
            tokenRepository.deleteByEmail(email);

            // Assert
            assertThat(tokenRepository.findByToken("token1")).isEmpty();
            assertThat(tokenRepository.findByToken("token2")).isEmpty();
            assertThat(tokenRepository.findByToken("token3")).isPresent();
        }
    }

    @Test
    @DisplayName("save - persists token with generated ID")
    void save_persistsTokenWithGeneratedId() {
        // Arrange
        PasswordResetToken token = PasswordResetToken.builder()
                .token("newtoken")
                .email("newuser@example.com")
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        // Act
        PasswordResetToken saved = tokenRepository.save(token);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getToken()).isEqualTo("newtoken");
    }

    @Test
    @DisplayName("save - updates existing token")
    void save_updatesExistingToken() {
        // Arrange
        PasswordResetToken token = PasswordResetToken.builder()
                .token("updatetoken")
                .email("user@example.com")
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();
        token = tokenRepository.save(token);

        // Act
        token.setUsed(true);
        PasswordResetToken updated = tokenRepository.save(token);

        // Assert
        assertThat(updated.isUsed()).isTrue();

        // Verify it was updated, not inserted
        Optional<PasswordResetToken> found = tokenRepository.findByToken("updatetoken");
        assertThat(found).isPresent();
        assertThat(found.get().isUsed()).isTrue();
    }
}
