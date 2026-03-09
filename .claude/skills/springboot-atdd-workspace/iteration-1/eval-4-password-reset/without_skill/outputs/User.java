package com.example.auth.domain.model;

/**
 * Domain entity representing a User in the system.
 */
public class User {

    private final Long id;
    private final String email;
    private final String passwordHash;

    public User(Long id, String email, String passwordHash) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // Getters

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    @Override
    public String toString() {
        return "User{" +
            "id=" + id +
            ", email='" + email + '\'' +
            '}';
    }
}
