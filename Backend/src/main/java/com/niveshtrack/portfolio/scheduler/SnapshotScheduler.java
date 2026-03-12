package com.niveshtrack.portfolio.scheduler;

import com.niveshtrack.portfolio.dto.response.HoldingDTO;
import com.niveshtrack.portfolio.entity.PortfolioSnapshot;
import com.niveshtrack.portfolio.entity.User;
import com.niveshtrack.portfolio.repository.SnapshotRepository;
import com.niveshtrack.portfolio.repository.UserRepository;
import com.niveshtrack.portfolio.service.HoldingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Creates end-of-day portfolio snapshots for all users every weekday at 4:00 PM IST.
 * Snapshots power the portfolio growth chart.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SnapshotScheduler {

    private final UserRepository userRepository;
    private final HoldingsService holdingsService;
    private final SnapshotRepository snapshotRepository;

    /**
     * Runs at 16:00 IST on Monday–Friday (after NSE market close at 15:30).
     * Zone = Asia/Kolkata ensures IST-aware scheduling.
     */
    @Scheduled(cron = "0 0 16 * * MON-FRI", zone = "Asia/Kolkata")
    @Transactional
    public void createDailySnapshots() {
        LocalDate today = LocalDate.now();
        List<User> users = userRepository.findAll();

        log.info("Creating daily portfolio snapshots for {} users on {}", users.size(), today);
        int created = 0;
        int skipped = 0;

        for (User user : users) {
            try {
                // Skip if a snapshot already exists for today
                Optional<PortfolioSnapshot> existing =
                        snapshotRepository.findByUserIdAndSnapshotDate(user.getId(), today);

                if (existing.isPresent()) {
                    skipped++;
                    continue;
                }

                // Calculate current portfolio value
                List<HoldingDTO> holdings = holdingsService.calculateHoldings(user.getId());

                BigDecimal totalValue = holdings.stream()
                        .map(HoldingDTO::getTotalValue)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalInvested = holdings.stream()
                        .map(HoldingDTO::getInvestedAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                PortfolioSnapshot snapshot = PortfolioSnapshot.builder()
                        .user(user)
                        .snapshotDate(today)
                        .totalValue(totalValue)
                        .totalInvested(totalInvested)
                        .build();

                snapshotRepository.save(snapshot);
                created++;

            } catch (Exception e) {
                log.error("Failed to create snapshot for userId={}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Snapshot job done: {} created, {} skipped for {}", created, skipped, today);
    }

    /**
     * Cleans up snapshots older than 2 years (optional maintenance job).
     * Runs at 2 AM on the 1st of each month.
     */
    @Scheduled(cron = "0 0 2 1 * *", zone = "Asia/Kolkata")
    @Transactional
    public void cleanupOldSnapshots() {
        // Retention policy: keep 2 years of daily snapshots
        // Implementation left for future enhancement
        log.debug("Snapshot cleanup job triggered (no-op in current version)");
    }
}
