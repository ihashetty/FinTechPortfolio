package com.niveshtrack.portfolio.repository;

import com.niveshtrack.portfolio.entity.PortfolioSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PortfolioSnapshot} entities.
 */
@Repository
public interface SnapshotRepository extends JpaRepository<PortfolioSnapshot, Long> {

    /** Snapshots for a user ordered by date descending */
    List<PortfolioSnapshot> findByUserIdOrderBySnapshotDateDesc(Long userId);

    /**
     * Snapshots for a user within a date range (used for growth charts).
     */
    List<PortfolioSnapshot> findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
            Long userId, LocalDate start, LocalDate end);

    /** Find the snapshot for a specific user on a specific date */
    Optional<PortfolioSnapshot> findByUserIdAndSnapshotDate(Long userId, LocalDate date);

    /**
     * Latest N snapshots for a user (using JPA derived query with Pageable would be ideal,
     * but this returns all ordered descending and callers slice as needed).
     */
    List<PortfolioSnapshot> findTop13ByUserIdOrderBySnapshotDateDesc(Long userId);
}
