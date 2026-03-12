package com.niveshtrack.portfolio.repository;

import com.niveshtrack.portfolio.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PriceAlert} entities.
 */
@Repository
public interface AlertRepository extends JpaRepository<PriceAlert, Long> {

    /** All alerts for a user ordered by creation date descending */
    List<PriceAlert> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Find an alert by ID and userId (ownership check) */
    Optional<PriceAlert> findByIdAndUserId(Long id, Long userId);

    /** All globally active alerts (used by alert checker scheduler) */
    List<PriceAlert> findByActiveTrue();

    /** Active alerts for a specific user */
    List<PriceAlert> findByUserIdAndActiveTrue(Long userId);

    /** Count active alerts per user */
    long countByUserIdAndActiveTrue(Long userId);
}
