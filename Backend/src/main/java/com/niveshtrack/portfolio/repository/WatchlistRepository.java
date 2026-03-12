package com.niveshtrack.portfolio.repository;

import com.niveshtrack.portfolio.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Watchlist} entities.
 */
@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {

    /** All watchlist items for a user ordered by creation date descending */
    List<Watchlist> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Find a watchlist entry by ID and userId (ownership check) */
    Optional<Watchlist> findByIdAndUserId(Long id, Long userId);

    /** Check if a stock is already in a user's watchlist */
    boolean existsByUserIdAndStockSymbol(Long userId, String stockSymbol);

    /** Remove all watchlist entries for a user (e.g., account deletion) */
    void deleteByUserId(Long userId);
}
