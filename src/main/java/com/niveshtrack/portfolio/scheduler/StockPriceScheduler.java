package com.niveshtrack.portfolio.scheduler;

import com.niveshtrack.portfolio.repository.StockRepository;
import com.niveshtrack.portfolio.service.StockPriceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;

/**
 * Scheduled task that polls the Alpha Vantage API for updated stock prices
 * during NSE market hours (9:15 AM – 3:30 PM IST).
 *
 * <p>Runs every 15 minutes. Outside market hours, the scheduler wakes up but
 * immediately returns without making any API calls.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StockPriceScheduler {

    private static final LocalTime MARKET_OPEN  = LocalTime.of(9, 15);
    private static final LocalTime MARKET_CLOSE = LocalTime.of(15, 30);

    private final StockRepository stockRepository;
    private final StockPriceService stockPriceService;

    /**
     * Runs every 10 minutes.
     * <ul>
     *   <li>During NSE market hours: fetches real prices from Alpha Vantage</li>
     *   <li>Outside market hours: runs simulation (±0.3% drift) to keep demo alive</li>
     * </ul>
     */
    @Scheduled(fixedRate = 60_000) // 1 minute
    public void updateStockPrices() {
        LocalTime now = LocalTime.now();

        List<String> symbols = stockRepository.findAllSymbols();
        if (symbols.isEmpty()) {
            log.debug("No symbols in the stocks table to update.");
            return;
        }

        if (now.isBefore(MARKET_OPEN) || now.isAfter(MARKET_CLOSE)) {
            log.debug("Outside market hours ({}) — running simulation for {} symbols", now, symbols.size());
            stockPriceService.updateStockPrices(symbols); // service handles simulation fallback
            return;
        }

        log.info("Market hours price update starting for {} symbols at {}", symbols.size(), now);
        stockPriceService.updateStockPrices(symbols);
        log.info("Market hours price update complete.");
    }
}
