package com.example.auth.repository;

import com.example.auth.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for UserRepository.
 * Tests database operations with a real PostgreSQL database using Testcontainers.
 *
 * Uses @DataJpaTest for testing JPA components in isolation.
 * Uses Testcontainers for real database testing (not H2).
 *
 * Acceptance Criteria:
 * [+] Email must be unique (database constraint)
 */
@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserRepository Tests")
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
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("findByEmail()")
    class FindByEmailMethod {

        @Test
        @DisplayName("should return user when email exists")
        void findByEmail_withExistingEmail_returnsUser() {
            // Arrange
            User user = new User("user@example.com", "encoded_password");
            entityManager.persistAndFlush(user);

            // Act
            Optional<User> found = userRepository.findByEmail("user@example.com");

            // Assert
            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("user@example.com");
            assertThat(found.get().getPassword()).isEqualTo("encoded_password");
        }

        @Test
        @DisplayName("should return empty when email does not exist")
        void findByEmail_withNonExistentEmail_returnsEmpty() {
            // Act
            Optional<User> found = userRepository.findByEmail("notfound@example.com");

            // Assert
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("should be case-sensitive for email lookup")
        void findByEmail_withDifferentCase_returnsEmpty() {
            // Arrange
            User user = new User("User@Example.com", "encoded_password");
            entityManager.persistAndFlush(user);

            // Act
            Optional<User> found = userRepository.findByEmail("user@example.com");

            // Assert
            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByEmail()")
    class ExistsByEmailMethod {

        @Test
        @DisplayName("should return true when email exists")
        void existsByEmail_withExistingEmail_returnsTrue() {
            // Arrange
            User user = new User("existing@example.com", "encoded_password");
            entityManager.persistAndFlush(user);

            // Act
            boolean exists = userRepository.existsByEmail("existing@example.com");

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("should return false when email does not exist")
        void existsByEmail_withNonExistentEmail_returnsFalse() {
            // Act
            boolean exists = userRepository.existsByEmail("notexisting@example.com");

            // Assert
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("Email Uniqueness")
    class EmailUniquenessConstraint {

        @Test
        @DisplayName("[-] should enforce unique constraint on email")
        void save_withDuplicateEmail_throwsConstraintViolation() {
            // Arrange
            User user1 = new User("duplicate@example.com", "password1");
            entityManager.persistAndFlush(user1);

            User user2 = new User("duplicate@example.com", "password2");

            // Act & Assert
            assertThatThrownBy(() -> {
                entityManager.persistAndFlush(user2);
            }).isInstanceOf(Exception.class);
        }
    }

    @Nested
    @DisplayName("save()")
    class SaveMethod {

        @Test
        @DisplayName("[+] should save user with generated id")
        void save_withValidUser_generatesId() {
            // Arrange
            User user = new User("newuser@example.com", "encoded_password");

            // Act
            User saved = userRepository.save(user);

            // Assert
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getId()).isPositive();
        }

        @Test
        @DisplayName("[+] should set created and updated timestamps")
        void save_withValidUser_setsTimestamps() {
            // Arrange
            User user = new User("timestamp@example.com", "encoded_password");

            // Act
            User saved = userRepository.save(user);

            // Assert
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should persist email and password correctly")
        void save_withValidUser_persistsData() {
            // Arrange
            User user = new User("persist@example.com", "my_encoded_password");

            // Act
            User saved = userRepository.save(user);
            entityManager.flush();
            entityManager.clear();

            // Assert
            User found = entityManager.find(User.class, saved.getId());
            assertThat(found.getEmail()).isEqualTo("persist@example.com");
            assertThat(found.getPassword()).isEqualTo("my_encoded_password");
        }
    }

    // Helper method for exception assertion
    private <T extends Throwable> org.assertj.core.api.ThrowableTypeAssert<T> assertThatThrownBy(
            org.assertj.core.api.ThrowingCallable shouldRaiseThrowable) {
        return org.assertj.core.api.Assertions.assertThatThrownBy(shouldRaiseThrowable);
    }
}
