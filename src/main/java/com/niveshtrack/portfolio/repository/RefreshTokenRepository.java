package com.niveshtrack.portfolio.repository;

import com.niveshtrack.portfolio.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link RefreshToken} entities.
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** Find a token by its string value */
    Optional<RefreshToken> findByToken(String token);

    /** Delete all refresh tokens for a user (logout all devices) */
    @Modifying
    @Transactional
    void deleteByUserId(Long userId);

    /** Delete a specific refresh token */
    @Modifying
    @Transactional
    void deleteByToken(String token);

    /** Clean up all expired tokens (called by scheduled job) */
    @Modifying
    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime dateTime);
}
