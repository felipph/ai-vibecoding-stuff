package com.example.user;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for UserRepository using Testcontainers with real PostgreSQL.
 *
 * This test class demonstrates ATDD principles:
 * - Tests are derived from acceptance criteria
 * - Tests verify real database behavior (not H2)
 * - Uses AssertJ fluent assertions
 * - Each test is independent and self-contained
 *
 * Acceptance Criteria:
 * [+] User can be found by exact email
 * [-] Non-existent email returns empty result
 * [+] Users can be searched by partial name match (firstName, lastName, or full name)
 * [+] Users can be searched by partial email match
 * [+] Users can be filtered by status
 * [+] Users can be filtered by registration date range
 * [+] Search results are ordered by registration date DESC, lastName ASC
 * [-] Empty criteria returns all users
 * [+] Multiple criteria can be combined
 */
@DataJpaTest
@Testcontainers
@DisplayName("UserRepository Integration Tests")
class UserRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        // Force PostgreSQL dialect to ensure native query compatibility
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    // Test data fixtures
    private User johnDoe;
    private User janeSmith;
    private User bobJohnson;
    private User aliceDoe;

    @BeforeEach
    void setUp() {
        // Clear database before each test for isolation
        userRepository.deleteAll();

        // Create test users with varied data
        johnDoe = createUser("John", "Doe", "john.doe@example.com", UserStatus.ACTIVE, LocalDate.of(2024, 1, 15));
        janeSmith = createUser("Jane", "Smith", "jane.smith@company.org", UserStatus.ACTIVE, LocalDate.of(2024, 2, 20));
        bobJohnson = createUser("Bob", "Johnson", "bob.j@test.net", UserStatus.INACTIVE, LocalDate.of(2023, 6, 10));
        aliceDoe = createUser("Alice", "Doe", "alice.doe@example.com", UserStatus.PENDING, LocalDate.of(2024, 3, 5));

        entityManager.flush();
        entityManager.clear();
    }

    // ============================================================
    // Acceptance Criterion: [+] User can be found by exact email
    // ============================================================

    @Test
    @DisplayName("findByEmail: with existing email returns user")
    void findByEmail_withExistingEmail_returnsUser() {
        // Act
        Optional<User> found = userRepository.findByEmail("john.doe@example.com");

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get())
                .extracting("firstName", "lastName", "email", "status")
                .containsExactly("John", "Doe", "john.doe@example.com", UserStatus.ACTIVE);
    }

    // ============================================================
    // Acceptance Criterion: [-] Non-existent email returns empty result
    // ============================================================

    @Test
    @DisplayName("findByEmail: with non-existent email returns empty")
    void findByEmail_withNonExistentEmail_returnsEmpty() {
        // Act
        Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertThat(found).isEmpty();
    }

    // ============================================================
    // Acceptance Criterion: [+] Users can be searched by partial name match
    // ============================================================

    @Nested
    @DisplayName("searchUsers: by name")
    class SearchByName {

        @Test
        @DisplayName("matches partial firstName")
        void searchUsers_byPartialFirstName_returnsMatchingUsers() {
            // Act
            List<User> results = userRepository.searchUsers("Joh", null, null, null, null);

            // Assert
            assertThat(results)
                    .hasSize(1)
                    .extracting(User::getFirstName)
                    .containsExactly("John");
        }

        @Test
        @DisplayName("matches partial lastName")
        void searchUsers_byPartialLastName_returnsMatchingUsers() {
            // Act
            List<User> results = userRepository.searchUsers("Smi", null, null, null, null);

            // Assert
            assertThat(results)
                    .hasSize(1)
                    .extracting(User::getLastName)
                    .containsExactly("Smith");
        }

        @Test
        @DisplayName("matches full name combination")
        void searchUsers_byFullName_returnsMatchingUsers() {
            // Act
            List<User> results = userRepository.searchUsers("John Doe", null, null, null, null);

            // Assert
            assertThat(results)
                    .hasSize(1)
                    .extracting(User::getEmail)
                    .containsExactly("john.doe@example.com");
        }

        @Test
        @DisplayName("is case-insensitive")
        void searchUsers_nameIsCaseInsensitive_returnsMatchingUsers() {
            // Act
            List<User> results = userRepository.searchUsers("JOHN", null, null, null, null);

            // Assert
            assertThat(results)
                    .hasSize(1)
                    .extracting(User::getFirstName)
                    .containsExactly("John");
        }

        @Test
        @DisplayName("matches multiple users with same lastName")
        void searchUsers_byLastName_returnsMultipleUsers() {
            // Act - Search for "Doe" should match both John Doe and Alice Doe
            List<User> results = userRepository.searchUsers("Doe", null, null, null, null);

            // Assert
            assertThat(results)
                    .hasSize(2)
                    .extracting(User::getLastName)
                    .containsOnly("Doe");
        }
    }

    // ============================================================
    // Acceptance Criterion: [+] Users can be searched by partial email match
    // ============================================================

    @Nested
    @DisplayName("searchUsers: by email")
    class SearchByEmail {

        @Test
        @DisplayName("matches partial email")
        void searchUsers_byPartialEmail_returnsMatchingUsers() {
            // Act
            List<User> results = userRepository.searchUsers(null, "example.com", null, null, null);

            // Assert
            assertThat(results)
                    .hasSize(2)
                    .extracting(User::getEmail)
                    .containsExactlyInAnyOrder("john.doe@example.com", "alice.doe@example.com");
        }

        @Test
        @DisplayName("is case-insensitive")
        void searchUsers_emailIsCaseInsensitive_returnsMatchingUsers() {
            // Act
            List<User> results = userRepository.searchUsers(null, "EXAMPLE.COM", null, null, null);

            // Assert
            assertThat(results)
                    .hasSize(2);
        }

        @Test
        @DisplayName("matches email username part")
        void searchUsers_byEmailUsername_returnsMatchingUsers() {
            // Act
            List<User> results = userRepository.searchUsers(null, "jane.smith", null, null, null);

            // Assert
            assertThat(results)
                    .hasSize(1)
                    .extracting(User::getEmail)
                    .containsExactly("jane.smith@company.org");
        }
    }

    // ============================================================
    // Acceptance Criterion: [+] Users can be filtered by status
    // ============================================================

    @Nested
    @DisplayName("searchUsers: by status")
    class SearchByStatus {

        @Test
        @DisplayName("filters by ACTIVE status")
        void searchUsers_byActiveStatus_returnsActiveUsers() {
            // Act
            List<User> results = userRepository.searchUsers(null, null, UserStatus.ACTIVE, null, null);

            // Assert
            assertThat(results)
                    .hasSize(2)
                    .extracting(User::getStatus)
                    .containsOnly(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("filters by INACTIVE status")
        void searchUsers_byInactiveStatus_returnsInactiveUsers() {
            // Act
            List<User> results = userRepository.searchUsers(null, null, UserStatus.INACTIVE, null, null);

            // Assert
            assertThat(results)
                    .hasSize(1)
                    .extracting(User::getEmail)
                    .containsExactly("bob.j@test.net");
        }

        @Test
        @DisplayName("filters by PENDING status")
        void searchUsers_byPendingStatus_returnsPendingUsers() {
            // Act
            List<User> results = userRepository.searchUsers(null, null, UserStatus.PENDING, null, null);

            // Assert
            assertThat(results)
                    .hasSize(1)
                    .extracting(User::getEmail)
                    .containsExactly("alice.doe@example.com");
        }
    }

    // ============================================================
    // Acceptance Criterion: [+] Users can be filtered by registration date range
    // ============================================================

    @Nested
    @DisplayName("searchUsers: by date range")
    class SearchByDateRange {

        @Test
        @DisplayName("filters by registrationDateFrom only")
        void searchUsers_byDateFrom_returnsUsersRegisteredAfterDate() {
            // Act - Users registered from Feb 1, 2024 onwards
            List<User> results = userRepository.searchUsers(
                    null, null, null,
                    LocalDate.of(2024, 2, 1),
                    null
            );

            // Assert - Should include Jane (Feb 20) and Alice (Mar 5)
            assertThat(results)
                    .hasSize(2)
                    .extracting(User::getEmail)
                    .containsExactlyInAnyOrder("jane.smith@company.org", "alice.doe@example.com");
        }

        @Test
        @DisplayName("filters by registrationDateTo only")
        void searchUsers_byDateTo_returnsUsersRegisteredBeforeDate() {
            // Act - Users registered up to Jan 31, 2024
            List<User> results = userRepository.searchUsers(
                    null, null, null,
                    null,
                    LocalDate.of(2024, 1, 31)
            );

            // Assert - Should include John (Jan 15) and Bob (Jun 2023)
            assertThat(results)
                    .hasSize(2)
                    .extracting(User::getEmail)
                    .containsExactlyInAnyOrder("john.doe@example.com", "bob.j@test.net");
        }

        @Test
        @DisplayName("filters by complete date range")
        void searchUsers_byDateRange_returnsUsersInDateRange() {
            // Act - Users registered between Jan 1 and Feb 28, 2024
            List<User> results = userRepository.searchUsers(
                    null, null, null,
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 2, 28)
            );

            // Assert - Should include John (Jan 15) and Jane (Feb 20)
            assertThat(results)
                    .hasSize(2)
                    .extracting(User::getEmail)
                    .containsExactlyInAnyOrder("john.doe@example.com", "jane.smith@company.org");
        }

        @Test
        @DisplayName("returns empty when no users in range")
        void searchUsers_byDateRangeWithNoMatches_returnsEmptyList() {
            // Act - Date range in 2025 where no users exist
            List<User> results = userRepository.searchUsers(
                    null, null, null,
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31)
            );

            // Assert
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("includes boundary dates (inclusive)")
        void searchUsers_dateRangeIsInclusive_includesBoundaryDates() {
            // Act - Exact date of John's registration
            List<User> results = userRepository.searchUsers(
                    null, null, null,
                    LocalDate.of(2024, 1, 15),
                    LocalDate.of(2024, 1, 15)
            );

            // Assert
            assertThat(results)
                    .hasSize(1)
                    .extracting(User::getEmail)
                    .containsExactly("john.doe@example.com");
        }
    }

    // ============================================================
    // Acceptance Criterion: [+] Search results are ordered correctly
    // ============================================================

    @Test
    @DisplayName("searchUsers: returns results ordered by registrationDate DESC, lastName ASC")
    void searchUsers_returnsOrderedResults() {
        // Act
        List<User> results = userRepository.searchUsers(null, null, null, null, null);

        // Assert - Order: Alice (Mar 5), Jane (Feb 20), John (Jan 15), Bob (Jun 2023)
        // For same date range, lastName would be used (but all dates are different here)
        assertThat(results)
                .hasSize(4)
                .extracting(User::getEmail)
                .containsExactly(
                        "alice.doe@example.com",  // Mar 5, 2024 (newest)
                        "jane.smith@company.org", // Feb 20, 2024
                        "john.doe@example.com",   // Jan 15, 2024
                        "bob.j@test.net"          // Jun 10, 2023 (oldest)
                );
    }

    // ============================================================
    // Acceptance Criterion: [-] Empty criteria returns all users
    // ============================================================

    @Test
    @DisplayName("searchUsers: with all null criteria returns all users")
    void searchUsers_withAllNullCriteria_returnsAllUsers() {
        // Act
        List<User> results = userRepository.searchUsers(null, null, null, null, null);

        // Assert
        assertThat(results).hasSize(4);
    }

    // ============================================================
    // Acceptance Criterion: [+] Multiple criteria can be combined
    // ============================================================

    @Nested
    @DisplayName("searchUsers: combined criteria")
    class SearchWithCombinedCriteria {

        @Test
        @DisplayName("combines name and status")
        void searchUsers_byNameAndStatus_returnsMatchingUsers() {
            // Act - Search for "Doe" with ACTIVE status
            List<User> results = userRepository.searchUsers("Doe", null, UserStatus.ACTIVE, null, null);

            // Assert - Only John Doe is ACTIVE (Alice Doe is PENDING)
            assertThat(results)
                    .hasSize(1)
                    .extracting(User::getEmail)
                    .containsExactly("john.doe@example.com");
        }

        @Test
        @DisplayName("combines email and status")
        void searchUsers_byEmailAndStatus_returnsMatchingUsers() {
            // Act - Search for "example.com" with PENDING status
            List<User> results = userRepository.searchUsers(null, "example.com", UserStatus.PENDING, null, null);

            // Assert - Only Alice Doe matches (john.doe is ACTIVE)
            assertThat(results)
                    .hasSize(1)
                    .extracting(User::getEmail)
                    .containsExactly("alice.doe@example.com");
        }

        @Test
        @DisplayName("combines name and date range")
        void searchUsers_byNameAndDateRange_returnsMatchingUsers() {
            // Act - Search for "Doe" in 2024
            List<User> results = userRepository.searchUsers(
                    "Doe", null, null,
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );

            // Assert - John and Alice Doe both registered in 2024
            assertThat(results)
                    .hasSize(2)
                    .extracting(User::getLastName)
                    .containsOnly("Doe");
        }

        @Test
        @DisplayName("combines all criteria")
        void searchUsers_withAllCriteria_returnsMatchingUsers() {
            // Act - Complex search: name contains "Doe", email contains "example", ACTIVE, in 2024
            List<User> results = userRepository.searchUsers(
                    "Doe",
                    "example",
                    UserStatus.ACTIVE,
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 31)
            );

            // Assert - Only John Doe matches all criteria
            assertThat(results)
                    .hasSize(1)
                    .extracting(User::getEmail)
                    .containsExactly("john.doe@example.com");
        }

        @Test
        @DisplayName("returns empty when combined criteria has no match")
        void searchUsers_withConflictingCriteria_returnsEmptyList() {
            // Act - Search for INACTIVE users with "example.com" email (no match)
            List<User> results = userRepository.searchUsers(
                    null, "example.com", UserStatus.INACTIVE, null, null
            );

            // Assert
            assertThat(results).isEmpty();
        }
    }

    // ============================================================
    // Acceptance Criterion: Using criteria object
    // ============================================================

    @Test
    @DisplayName("searchUsers: with criteria object returns correct results")
    void searchUsers_withCriteriaObject_returnsMatchingUsers() {
        // Arrange
        UserSearchCriteria criteria = UserSearchCriteria.builder()
                .name("Jane")
                .build();

        // Act
        List<User> results = userRepository.searchUsers(criteria);

        // Assert
        assertThat(results)
                .hasSize(1)
                .extracting(User::getFirstName)
                .containsExactly("Jane");
    }

    // ============================================================
    // Count tests
    // ============================================================

    @Test
    @DisplayName("countSearchResults: returns correct count")
    void countSearchResults_withCriteria_returnsCorrectCount() {
        // Act
        long count = userRepository.countSearchResults("Doe", null, null, null, null);

        // Assert
        assertThat(count).isEqualTo(2);
    }

    // ============================================================
    // Helper methods
    // ============================================================

    private User createUser(String firstName, String lastName, String email, UserStatus status, LocalDate registrationDate) {
        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password("encodedPassword123")
                .status(status)
                .registrationDate(registrationDate)
                .build();
        return entityManager.persistAndFlush(user);
    }
}
