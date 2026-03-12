package com.niveshtrack.portfolio.scheduler;

import com.niveshtrack.portfolio.entity.AlertDirection;
import com.niveshtrack.portfolio.entity.PriceAlert;
import com.niveshtrack.portfolio.repository.AlertRepository;
import com.niveshtrack.portfolio.service.StockPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Checks all active price alerts every 5 minutes against the current market price.
 * Triggers (deactivates) alerts when the price condition is met.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AlertCheckerScheduler {

    private final AlertRepository alertRepository;
    private final StockPriceService stockPriceService;

    /**
     * Runs every 5 minutes.
     */
    @Scheduled(fixedRate = 300_000) // 5 minutes
    @Transactional
    public void checkAlerts() {
        List<PriceAlert> activeAlerts = alertRepository.findByActiveTrue();

        if (activeAlerts.isEmpty()) {
            return;
        }

        log.debug("Checking {} active price alerts", activeAlerts.size());
        int triggered = 0;

        for (PriceAlert alert : activeAlerts) {
            try {
                BigDecimal currentPrice = stockPriceService.fetchCurrentPrice(alert.getStockSymbol());

                if (currentPrice == null) continue;

                boolean shouldTrigger = false;
                if (alert.getDirection() == AlertDirection.ABOVE
                        && currentPrice.compareTo(alert.getTargetPrice()) >= 0) {
                    shouldTrigger = true;
                } else if (alert.getDirection() == AlertDirection.BELOW
                        && currentPrice.compareTo(alert.getTargetPrice()) <= 0) {
                    shouldTrigger = true;
                }

                if (shouldTrigger) {
                    alert.setActive(false);
                    alert.setTriggeredAt(LocalDateTime.now());
                    alertRepository.save(alert);
                    triggered++;

                    log.info("ALERT TRIGGERED — userId={}, stock={}, direction={}, target={}, current={}",
                            alert.getUser().getId(),
                            alert.getStockSymbol(),
                            alert.getDirection(),
                            alert.getTargetPrice(),
                            currentPrice);

                    // TODO: Integrate email/push notification here
                }

            } catch (Exception e) {
                log.error("Error checking alert id={}: {}", alert.getId(), e.getMessage());
            }
        }

        if (triggered > 0) {
            log.info("Alert check complete: {}/{} alerts triggered", triggered, activeAlerts.size());
        }
    }
}
