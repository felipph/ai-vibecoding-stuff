package com.example.auth.domain.repository;

import com.example.auth.domain.model.User;

import java.util.Optional;

/**
 * Repository interface for User persistence operations.
 */
public interface UserRepository {

    /**
     * Finds a user by email address.
     *
     * @param email the email to search for
     * @return Optional containing the user if found
     */
    Optional<User> findByEmail(String email);

    /**
     * Finds a user by ID.
     *
     * @param id the user ID
     * @return Optional containing the user if found
     */
    Optional<User> findById(Long id);

    /**
     * Updates a user's password.
     *
     * @param userId the user ID
     * @param newPassword the new password (will be hashed)
     */
    void updatePassword(Long userId, String newPassword);

    /**
     * Saves a new user.
     *
     * @param user the user to save
     * @return the saved user with generated ID
     */
    User save(User user);
}
