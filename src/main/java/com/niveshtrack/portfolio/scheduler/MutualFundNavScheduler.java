package com.niveshtrack.portfolio.scheduler;

import com.niveshtrack.portfolio.entity.MutualFund;
import com.niveshtrack.portfolio.repository.MutualFundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Simulates daily NAV drift for all mutual funds.
 *
 * <p>Runs every day at 8:00 PM IST (after market close).
 * Each fund's NAV is adjusted by a random percentage between –0.5% and +0.5%.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MutualFundNavScheduler {

    private final MutualFundRepository mutualFundRepository;

    /**
     * NAV simulation every 60 seconds for live demo experience.
     */
    @Scheduled(fixedRate = 60_000)
    public void updateNavs() {
        List<MutualFund> funds = mutualFundRepository.findAll();
        if (funds.isEmpty()) {
            log.debug("No mutual funds in the database to update.");
            return;
        }

        log.info("Starting NAV simulation for {} mutual funds", funds.size());

        for (MutualFund fund : funds) {
            BigDecimal oldNav = fund.getNav();
            if (oldNav == null || oldNav.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            // Random drift between -0.75% and +0.75%
            double driftPct = ThreadLocalRandom.current().nextDouble(-0.0075, 0.0075);
            BigDecimal multiplier = BigDecimal.ONE.add(BigDecimal.valueOf(driftPct));
            BigDecimal newNav = oldNav.multiply(multiplier).setScale(4, RoundingMode.HALF_UP);

            // Ensure NAV doesn't go below 1.0
            if (newNav.compareTo(BigDecimal.ONE) < 0) {
                newNav = BigDecimal.ONE;
            }

            fund.setNav(newNav);
            fund.setLastUpdated(LocalDateTime.now());

            log.debug("NAV {} : {} → {} (drift {:.4f}%)", fund.getSymbol(), oldNav, newNav,
                    BigDecimal.valueOf(driftPct * 100).setScale(4, RoundingMode.HALF_UP));
        }

        mutualFundRepository.saveAll(funds);
        log.info("NAV simulation complete for {} funds", funds.size());
    }
}
