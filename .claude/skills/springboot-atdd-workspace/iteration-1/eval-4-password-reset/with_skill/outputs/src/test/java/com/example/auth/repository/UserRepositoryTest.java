package com.example.auth.repository;

import com.example.auth.model.User;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ATDD Repository tests for User entity.
 * Uses Testcontainers for real database testing.
 */
@DataJpaTest
@Testcontainers
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("with existing email - returns user")
        void withExistingEmail_returnsUser() {
            // Arrange
            User user = User.builder()
                    .email("user@example.com")
                    .password("encodedPassword")
                    .build();
            userRepository.save(user);

            // Act
            Optional<User> found = userRepository.findByEmail("user@example.com");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("user@example.com");
            assertThat(found.get().getPassword()).isEqualTo("encodedPassword");
        }

        @Test
        @DisplayName("with non-existent email - returns empty")
        void withNonExistentEmail_returnsEmpty() {
            // Act
            Optional<User> found = userRepository.findByEmail("notfound@example.com");

            // Assert
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByEmail")
    class ExistsByEmail {

        @Test
        @DisplayName("with existing email - returns true")
        void withExistingEmail_returnsTrue() {
            // Arrange
            User user = User.builder()
                    .email("existing@example.com")
                    .password("encodedPassword")
                    .build();
            userRepository.save(user);

            // Act
            boolean exists = userRepository.existsByEmail("existing@example.com");

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("with non-existent email - returns false")
        void withNonExistentEmail_returnsFalse() {
            // Act
            boolean exists = userRepository.existsByEmail("notfound@example.com");

            // Assert
            assertThat(exists).isFalse();
        }
    }

    @Test
    @DisplayName("save - persists user with generated ID")
    void save_persistsUserWithGeneratedId() {
        // Arrange
        User user = User.builder()
                .email("newuser@example.com")
                .password("encodedPassword")
                .build();

        // Act
        User saved = userRepository.save(user);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getEmail()).isEqualTo("newuser@example.com");
    }
}
