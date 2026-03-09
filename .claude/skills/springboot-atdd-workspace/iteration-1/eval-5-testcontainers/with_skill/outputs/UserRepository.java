package com.example.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity with custom JPQL queries.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email.
     *
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Complex search query that filters users by multiple criteria.
     * All parameters are optional - null values are ignored.
     *
     * This query demonstrates:
     * - Dynamic conditions with COALESCE for optional parameters
     * - ILIKE for case-insensitive partial matching on name and email
     * - Date range filtering for registration date
     * - Status filtering with enum comparison
     *
     * @param name partial name to search (matches firstName or lastName)
     * @param email partial email to search
     * @param status user status to filter by
     * @param registrationDateFrom start of registration date range
     * @param registrationDateTo end of registration date range
     * @return list of matching users
     */
    @Query("""
        SELECT u FROM User u
        WHERE (:name IS NULL
               OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))
               OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
               OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:email IS NULL
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
          AND (:status IS NULL
               OR u.status = :status)
          AND (:registrationDateFrom IS NULL
               OR u.registrationDate >= :registrationDateFrom)
          AND (:registrationDateTo IS NULL
               OR u.registrationDate <= :registrationDateTo)
        ORDER BY u.registrationDate DESC, u.lastName ASC
        """)
    List<User> searchUsers(
            @Param("name") String name,
            @Param("email") String email,
            @Param("status") UserStatus status,
            @Param("registrationDateFrom") LocalDate registrationDateFrom,
            @Param("registrationDateTo") LocalDate registrationDateTo
    );

    /**
     * Alternative method using criteria object for cleaner API.
     *
     * @param criteria the search criteria
     * @return list of matching users
     */
    default List<User> searchUsers(UserSearchCriteria criteria) {
        return searchUsers(
                criteria.getName(),
                criteria.getEmail(),
                criteria.getStatus(),
                criteria.getRegistrationDateFrom(),
                criteria.getRegistrationDateTo()
        );
    }

    /**
     * Count users matching the search criteria.
     *
     * @param name partial name to search
     * @param email partial email to search
     * @param status user status to filter by
     * @param registrationDateFrom start of registration date range
     * @param registrationDateTo end of registration date range
     * @return count of matching users
     */
    @Query("""
        SELECT COUNT(u) FROM User u
        WHERE (:name IS NULL
               OR LOWER(CONCAT(u.firstName, ' ', u.lastName)) LIKE LOWER(CONCAT('%', :name, '%'))
               OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%'))
               OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%')))
          AND (:email IS NULL
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))
          AND (:status IS NULL
               OR u.status = :status)
          AND (:registrationDateFrom IS NULL
               OR u.registrationDate >= :registrationDateFrom)
          AND (:registrationDateTo IS NULL
               OR u.registrationDate <= :registrationDateTo)
        """)
    long countSearchResults(
            @Param("name") String name,
            @Param("email") String email,
            @Param("status") UserStatus status,
            @Param("registrationDateFrom") LocalDate registrationDateFrom,
            @Param("registrationDateTo") LocalDate registrationDateTo
    );
}
