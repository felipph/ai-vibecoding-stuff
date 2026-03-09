package com.example.user;

import lombok.*;

import java.time.LocalDate;

/**
 * Criteria object for searching users with multiple filters.
 * All fields are optional - null values are ignored in the search.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSearchCriteria {

    /**
     * Filter by name (partial match on firstName or lastName)
     */
    private String name;

    /**
     * Filter by email (partial match)
     */
    private String email;

    /**
     * Filter by user status
     */
    private UserStatus status;

    /**
     * Filter by registration date (from this date onwards)
     */
    private LocalDate registrationDateFrom;

    /**
     * Filter by registration date (up to this date)
     */
    private LocalDate registrationDateTo;
}
