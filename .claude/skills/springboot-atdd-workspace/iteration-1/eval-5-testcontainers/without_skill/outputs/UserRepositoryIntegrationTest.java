package com.example.user.repository;

import com.example.config.TestcontainersConfiguration;
import com.example.user.domain.User;
import com.example.user.domain.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for UserRepository using Testcontainers with real PostgreSQL.
 *
 * This test class demonstrates:
 * - Using @DataJpaTest for JPA slice testing
 * - Testcontainers for real PostgreSQL database
 * - Testing complex JPQL queries with multiple criteria
 * - Testing pagination and sorting
 * - Testing native queries
 *
 * Configuration:
 * - Uses TestcontainersConfiguration for PostgreSQL container
 * - @AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
 *   is implicit with @ServiceConnection
 */
@DataJpaTest
@Import(TestcontainersConfiguration.class)
@Testcontainers
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User activeUser1;
    private User activeUser2;
    private User inactiveUser;
    private User suspendedUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create test users with different attributes
        activeUser1 = User.create(
            "João Silva",
            "joao.silva@example.com",
            UserStatus.ACTIVE,
            LocalDate.of(2024, 1, 15)
        );

        activeUser2 = User.create(
            "Maria Santos",
            "maria.santos@example.com",
            UserStatus.ACTIVE,
            LocalDate.of(2024, 2, 20)
        );

        inactiveUser = User.create(
            "Pedro Oliveira",
            "pedro.oliveira@example.com",
            UserStatus.INACTIVE,
            LocalDate.of(2024, 3, 10)
        );

        suspendedUser = User.create(
            "Ana Costa",
            "ana.costa@example.com",
            UserStatus.SUSPENDED,
            LocalDate.of(2024, 1, 5)
        );

        // Persist test data
        entityManager.persist(activeUser1);
        entityManager.persist(activeUser2);
        entityManager.persist(inactiveUser);
        entityManager.persist(suspendedUser);
        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("Basic Finder Methods")
    class BasicFinderMethods {

        @Test
        @DisplayName("Should find user by email")
        void shouldFindByEmail() {
            // When
            Optional<User> found = userRepository.findByEmail("joao.silva@example.com");

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("João Silva");
            assertThat(found.get().getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should return empty when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            // When
            Optional<User> found = userRepository.findByEmail("nonexistent@example.com");

            // Then
            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should find all users by status")
        void shouldFindByStatus() {
            // When
            List<User> activeUsers = userRepository.findByStatus(UserStatus.ACTIVE);

            // Then
            assertThat(activeUsers).hasSize(2);
            assertThat(activeUsers).extracting("name")
                .containsExactlyInAnyOrder("João Silva", "Maria Santos");
        }
    }

    @Nested
    @DisplayName("Complex JPQL Query - Multiple Criteria")
    class MultipleCriteriaQuery {

        @Test
        @DisplayName("Should filter by name only")
        void shouldFilterByNameOnly() {
            // When
            List<User> results = userRepository.findByMultipleCriteria(
                "Silva", null, null, null, null
            );

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("Should filter by email pattern")
        void shouldFilterByEmailPattern() {
            // When
            List<User> results = userRepository.findByMultipleCriteria(
                null, "example.com", null, null, null
            );

            // Then
            assertThat(results).hasSize(4);
        }

        @Test
        @DisplayName("Should filter by status only")
        void shouldFilterByStatusOnly() {
            // When
            List<User> results = userRepository.findByMultipleCriteria(
                null, null, UserStatus.ACTIVE, null, null
            );

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).allMatch(u -> u.getStatus() == UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should filter by date range")
        void shouldFilterByDateRange() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);

            // When
            List<User> results = userRepository.findByMultipleCriteria(
                null, null, null, startDate, endDate
            );

            // Then
            assertThat(results).hasSize(2);
            assertThat(results).extracting("name")
                .containsExactlyInAnyOrder("João Silva", "Ana Costa");
        }

        @Test
        @DisplayName("Should filter by name and status")
        void shouldFilterByNameAndStatus() {
            // When
            List<User> results = userRepository.findByMultipleCriteria(
                "Maria", null, UserStatus.ACTIVE, null, null
            );

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("Maria Santos");
        }

        @Test
        @DisplayName("Should filter by all criteria")
        void shouldFilterByAllCriteria() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            // When
            List<User> results = userRepository.findByMultipleCriteria(
                "João", "silva", UserStatus.ACTIVE, startDate, endDate
            );

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getEmail()).isEqualTo("joao.silva@example.com");
        }

        @Test
        @DisplayName("Should return all users when all criteria are null")
        void shouldReturnAllWhenAllCriteriaNull() {
            // When
            List<User> results = userRepository.findByMultipleCriteria(
                null, null, null, null, null
            );

            // Then
            assertThat(results).hasSize(4);
        }

        @Test
        @DisplayName("Should be case-insensitive for name search")
        void shouldBeCaseInsensitiveForName() {
            // When
            List<User> results = userRepository.findByMultipleCriteria(
                "JOÃO", null, null, null, null
            );

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("Should be case-insensitive for email search")
        void shouldBeCaseInsensitiveForEmail() {
            // When
            List<User> results = userRepository.findByMultipleCriteria(
                null, "MARIA.SANTOS@EXAMPLE.COM", null, null, null
            );

            // Then
            assertThat(results).hasSize(1);
        }

        @Test
        @DisplayName("Should support partial matches")
        void shouldSupportPartialMatches() {
            // When
            List<User> results = userRepository.findByMultipleCriteria(
                "Sil", null, null, null, null
            );

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("João Silva");
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {

        @Test
        @DisplayName("Should paginate results")
        void shouldPaginateResults() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 2);

            // When
            Page<User> page = userRepository.findByMultipleCriteriaPaginated(
                null, null, null, null, null, pageRequest
            );

            // Then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(4);
            assertThat(page.getTotalPages()).isEqualTo(2);
            assertThat(page.isFirst()).isTrue();
            assertThat(page.hasNext()).isTrue();
        }

        @Test
        @DisplayName("Should return second page")
        void shouldReturnSecondPage() {
            // Given
            PageRequest pageRequest = PageRequest.of(1, 2);

            // When
            Page<User> page = userRepository.findByMultipleCriteriaPaginated(
                null, null, null, null, null, pageRequest
            );

            // Then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getNumber()).isEqualTo(1);
            assertThat(page.isLast()).isTrue();
        }

        @Test
        @DisplayName("Should sort results by name ascending")
        void shouldSortResultsByNameAscending() {
            // Given
            PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("name").ascending());

            // When
            Page<User> page = userRepository.findByMultipleCriteriaPaginated(
                null, null, UserStatus.ACTIVE, null, null, pageRequest
            );

            // Then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getContent().get(0).getName()).isEqualTo("João Silva");
            assertThat(page.getContent().get(1).getName()).isEqualTo("Maria Santos");
        }

        @Test
        @DisplayName("Should paginate filtered results")
        void shouldPaginateFilteredResults() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);
            PageRequest pageRequest = PageRequest.of(0, 5);

            // When
            Page<User> page = userRepository.findByMultipleCriteriaPaginated(
                null, null, null, startDate, endDate, pageRequest
            );

            // Then
            assertThat(page.getContent()).hasSize(2);
            assertThat(page.getTotalElements()).isEqualTo(2);
            assertThat(page.getTotalPages()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Count Queries")
    class CountQueries {

        @Test
        @DisplayName("Should count by status")
        void shouldCountByStatus() {
            // When
            long count = userRepository.countByCriteria(UserStatus.ACTIVE, null, null);

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should count by date range")
        void shouldCountByDateRange() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 2, 1);
            LocalDate endDate = LocalDate.of(2024, 3, 31);

            // When
            long count = userRepository.countByCriteria(null, startDate, endDate);

            // Then
            assertThat(count).isEqualTo(2);
        }

        @Test
        @DisplayName("Should count all when criteria is null")
        void shouldCountAllWhenCriteriaNull() {
            // When
            long count = userRepository.countByCriteria(null, null, null);

            // Then
            assertThat(count).isEqualTo(4);
        }
    }

    @Nested
    @DisplayName("Search Functionality")
    class SearchFunctionality {

        @Test
        @DisplayName("Should search by name")
        void shouldSearchByName() {
            // When
            List<User> results = userRepository.searchByNameOrEmail("Pedro");

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("Pedro Oliveira");
        }

        @Test
        @DisplayName("Should search by email")
        void shouldSearchByEmail() {
            // When
            List<User> results = userRepository.searchByNameOrEmail("maria.santos");

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getEmail()).isEqualTo("maria.santos@example.com");
        }

        @Test
        @DisplayName("Should return multiple matches")
        void shouldReturnMultipleMatches() {
            // When
            List<User> results = userRepository.searchByNameOrEmail("example");

            // Then
            assertThat(results).hasSize(4);
        }
    }

    @Nested
    @DisplayName("Status-based Queries")
    class StatusBasedQueries {

        @Test
        @DisplayName("Should find by status and date range")
        void shouldFindByStatusAndDateRange() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 1, 31);

            // When
            List<User> results = userRepository.findByStatusAndRegistrationDateBetween(
                UserStatus.ACTIVE, startDate, endDate
            );

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getName()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("Should find by multiple statuses")
        void shouldFindByMultipleStatuses() {
            // Given
            List<UserStatus> statuses = List.of(UserStatus.ACTIVE, UserStatus.INACTIVE);
            LocalDate startDate = LocalDate.of(2024, 1, 1);

            // When
            List<User> results = userRepository.findByStatusesAndDateAfter(statuses, startDate);

            // Then
            assertThat(results).hasSize(3);
            assertThat(results)
                .extracting("status")
                .containsOnly(UserStatus.ACTIVE, UserStatus.INACTIVE);
        }
    }

    @Nested
    @DisplayName("Native Query Tests")
    class NativeQueryTests {

        @Test
        @DisplayName("Should execute native query with limit")
        void shouldExecuteNativeQueryWithLimit() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            // When
            List<User> results = userRepository.findRecentUsersByStatusNative(
                "ACTIVE", startDate, endDate, 1
            );

            // Then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getStatus()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should respect limit in native query")
        void shouldRespectLimitInNativeQuery() {
            // Given
            LocalDate startDate = LocalDate.of(2024, 1, 1);
            LocalDate endDate = LocalDate.of(2024, 12, 31);

            // When
            List<User> results = userRepository.findRecentUsersByStatusNative(
                "ACTIVE", startDate, endDate, 10
            );

            // Then
            assertThat(results).hasSize(2); // Only 2 active users exist
        }
    }

    @Nested
    @DisplayName("Email Existence Check")
    class EmailExistenceCheck {

        @Test
        @DisplayName("Should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            // When
            boolean exists = userRepository.existsByEmailIgnoreCase("JOAO.SILVA@EXAMPLE.COM");

            // Then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            // When
            boolean exists = userRepository.existsByEmailIgnoreCase("nonexistent@example.com");

            // Then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Should be case-insensitive")
        void shouldBeCaseInsensitive() {
            // When
            boolean exists = userRepository.existsByEmailIgnoreCase("MARIA.SANTOS@EXAMPLE.COM");

            // Then
            assertThat(exists).isTrue();
        }
    }

    @Nested
    @DisplayName("Recent Registration Tests")
    class RecentRegistrationTests {

        @Test
        @DisplayName("Should find recently registered users by status")
        void shouldFindRecentlyRegisteredByStatus() {
            // Given - create a user registered "yesterday"
            User recentUser = User.create(
                "Recent User",
                "recent@example.com",
                UserStatus.ACTIVE,
                LocalDate.now().minusDays(5)
            );
            entityManager.persist(recentUser);
            entityManager.flush();

            // When
            List<User> results = userRepository.findRecentlyRegisteredByStatus(
                UserStatus.ACTIVE, 10
            );

            // Then
            assertThat(results).isNotEmpty();
            assertThat(results)
                .extracting("status")
                .containsOnly(UserStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("Should handle empty result set")
        void shouldHandleEmptyResultSet() {
            // When
            List<User> results = userRepository.findByMultipleCriteria(
                "NonExistentName", null, null, null, null
            );

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should handle date range with no matches")
        void shouldHandleDateRangeWithNoMatches() {
            // Given
            LocalDate startDate = LocalDate.of(2020, 1, 1);
            LocalDate endDate = LocalDate.of(2020, 12, 31);

            // When
            List<User> results = userRepository.findByMultipleCriteria(
                null, null, null, startDate, endDate
            );

            // Then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("Should handle special characters in search")
        void shouldHandleSpecialCharactersInSearch() {
            // When
            List<User> results = userRepository.findByMultipleCriteria(
                "%", null, null, null, null
            );

            // Then - should not throw exception and return appropriate results
            assertThat(results).isNotNull();
        }

        @Test
        @DisplayName("Should handle very long search terms")
        void shouldHandleVeryLongSearchTerms() {
            // Given
            String longTerm = "a".repeat(1000);

            // When
            List<User> results = userRepository.findByMultipleCriteria(
                longTerm, null, null, null, null
            );

            // Then
            assertThat(results).isEmpty();
        }
    }

    @Nested
    @DisplayName("Performance and Ordering")
    class PerformanceAndOrdering {

        @Test
        @DisplayName("Should return results in correct order - registration date desc, name asc")
        void shouldReturnResultsInCorrectOrder() {
            // When
            List<User> results = userRepository.findByMultipleCriteria(
                null, null, null, null, null
            );

            // Then
            assertThat(results).hasSize(4);
            // Most recent first (registrationDate DESC)
            assertThat(results.get(0).getRegistrationDate())
                .isAfterOrEqualTo(results.get(1).getRegistrationDate());
        }

        @Test
        @DisplayName("Should handle large result sets efficiently")
        void shouldHandleLargeResultSetsEfficiently() {
            // Given - create additional users
            for (int i = 0; i < 50; i++) {
                User user = User.create(
                    "User " + i,
                    "user" + i + "@example.com",
                    UserStatus.ACTIVE,
                    LocalDate.now().minusDays(i)
                );
                entityManager.persist(user);
            }
            entityManager.flush();

            // When
            long startTime = System.currentTimeMillis();
            List<User> results = userRepository.findByMultipleCriteria(
                null, null, UserStatus.ACTIVE, null, null
            );
            long duration = System.currentTimeMillis() - startTime;

            // Then
            assertThat(results).hasSize(52); // 2 original + 50 new
            assertThat(duration).isLessThan(1000); // Should complete in less than 1 second
        }
    }
}
