package com.investtrack.auth.repository;

import com.investtrack.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for User entity.
 * Provides lookup methods for authentication and registration flows.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    /**
     * Find a user by their username (used during login).
     */
    Optional<User> findByUsername(String username);

    /**
     * Find a user by their email address.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if a username is already taken (used during registration).
     */
    boolean existsByUsername(String username);

    /**
     * Check if an email is already registered (used during registration).
     */
    boolean existsByEmail(String email);
}
