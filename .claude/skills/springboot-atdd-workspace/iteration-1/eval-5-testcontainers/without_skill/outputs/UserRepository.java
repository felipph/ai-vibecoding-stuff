package com.example.user.repository;

import com.example.user.domain.User;
import com.example.user.domain.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity with complex JPQL queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Basic finder methods
    Optional<User> findByEmail(String email);

    List<User> findByStatus(UserStatus status);

    // Complex JPQL query with multiple criteria
    @Query("""
        SELECT u FROM User u
        WHERE (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
          AND (:status IS NULL OR u.status = :status)
          AND (:startDate IS NULL OR u.registrationDate >= :startDate)
          AND (:endDate IS NULL OR u.registrationDate <= :endDate)
        ORDER BY u.registrationDate DESC, u.name ASC
        """)
    List<User> findByMultipleCriteria(
        @Param("name") String name,
        @Param("email") String email,
        @Param("status") UserStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Complex JPQL query with pagination
    @Query("""
        SELECT u FROM User u
        WHERE (:name IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
          AND (:status IS NULL OR u.status = :status)
          AND (:startDate IS NULL OR u.registrationDate >= :startDate)
          AND (:endDate IS NULL OR u.registrationDate <= :endDate)
        """)
    Page<User> findByMultipleCriteriaPaginated(
        @Param("name") String name,
        @Param("email") String email,
        @Param("status") UserStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        Pageable pageable
    );

    // Count by criteria
    @Query("""
        SELECT COUNT(u) FROM User u
        WHERE (:status IS NULL OR u.status = :status)
          AND (:startDate IS NULL OR u.registrationDate >= :startDate)
          AND (:endDate IS NULL OR u.registrationDate <= :endDate)
        """)
    long countByCriteria(
        @Param("status") UserStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Find active users registered within a date range
    @Query("""
        SELECT u FROM User u
        WHERE u.status = :status
          AND u.registrationDate BETWEEN :startDate AND :endDate
        ORDER BY u.registrationDate ASC
        """)
    List<User> findByStatusAndRegistrationDateBetween(
        @Param("status") UserStatus status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

    // Search users by name or email (partial match, case-insensitive)
    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
        ORDER BY u.name ASC
        """)
    List<User> searchByNameOrEmail(@Param("searchTerm") String searchTerm);

    // Find users with multiple statuses
    @Query("""
        SELECT u FROM User u
        WHERE u.status IN :statuses
          AND (:startDate IS NULL OR u.registrationDate >= :startDate)
        ORDER BY u.registrationDate DESC
        """)
    List<User> findByStatusesAndDateAfter(
        @Param("statuses") List<UserStatus> statuses,
        @Param("startDate") LocalDate startDate
    );

    // Native query example for complex database-specific operations
    @Query(
        value = """
            SELECT * FROM users u
            WHERE u.status = CAST(:status AS VARCHAR)
              AND u.registration_date >= :startDate
              AND u.registration_date <= :endDate
            ORDER BY u.registration_date DESC
            LIMIT :limit
            """,
        nativeQuery = true
    )
    List<User> findRecentUsersByStatusNative(
        @Param("status") String status,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("limit") int limit
    );

    // Check if email exists (case-insensitive)
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE LOWER(u.email) = LOWER(:email)")
    boolean existsByEmailIgnoreCase(@Param("email") String email);

    // Find users registered in the last N days with specific status
    @Query("""
        SELECT u FROM User u
        WHERE u.status = :status
          AND u.registrationDate >= CURRENT_DATE - :days
        ORDER BY u.registrationDate DESC
        """)
    List<User> findRecentlyRegisteredByStatus(
        @Param("status") UserStatus status,
        @Param("days") int days
    );
}
