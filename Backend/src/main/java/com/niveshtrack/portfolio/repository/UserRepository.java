package com.niveshtrack.portfolio.repository;

import com.niveshtrack.portfolio.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entities.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their email address (used for authentication).
     */
    Optional<User> findByEmail(String email);

    /**
     * Check whether a user with the given email already exists.
     */
    Boolean existsByEmail(String email);
}
