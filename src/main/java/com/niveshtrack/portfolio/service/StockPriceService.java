package com.niveshtrack.portfolio.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.niveshtrack.portfolio.entity.AssetPriceHistory;
import com.niveshtrack.portfolio.entity.AssetType;
import com.niveshtrack.portfolio.entity.Stock;
import com.niveshtrack.portfolio.repository.AssetPriceHistoryRepository;
import com.niveshtrack.portfolio.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Fetches current stock prices from the Alpha Vantage API, updates the stocks table,
 * and records price history. Falls back to simulation if API call fails.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockPriceService {

    @Value("${stock-api.alpha-vantage.key}")
    private String apiKey;

    @Value("${stock-api.alpha-vantage.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;
    private final StockRepository stockRepository;
    private final AssetPriceHistoryRepository priceHistoryRepository;

    /**
     * Fetches the current price for a symbol.
     * Results are cached for 15 minutes to reduce API calls.
     *
     * @param symbol NSE/BSE ticker (e.g., "RELIANCE")
     * @return current price, or null on failure
     */
    @Cacheable(value = "stockPrices", key = "#symbol", unless = "#result == null")
    public BigDecimal fetchCurrentPrice(String symbol) {
        try {
            // Alpha Vantage: NSE symbols require ".NSE" suffix
            String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s.NSE&apikey=%s",
                    baseUrl, symbol, apiKey);

            JsonNode response = restTemplate.getForObject(url, JsonNode.class);

            if (response != null && response.has("Global Quote")) {
                JsonNode quote = response.get("Global Quote");
                String priceStr = quote.path("05. price").asText();
                if (priceStr != null && !priceStr.isBlank() && !priceStr.equals("0")) {
                    log.debug("Fetched price for {}: {}", symbol, priceStr);
                    return new BigDecimal(priceStr);
                }
            }

            log.warn("No price data returned for symbol: {}", symbol);
            return null;

        } catch (Exception e) {
            log.error("Error fetching price for {}: {}", symbol, e.getMessage());
            return null;
        }
    }

    /**
     * Batch update prices for multiple symbols.
     * Falls back to simulation if API returns null.
     * Records price history for every update.
     *
     * @param symbols list of stock symbols to update
     */
    @CacheEvict(value = "stockPrices", allEntries = true)
    public void updateStockPrices(List<String> symbols) {
        log.info("Starting price update for {} symbols", symbols.size());
        int updated = 0;

        for (String symbol : symbols) {
            BigDecimal price = fetchCurrentPrice(symbol);
            String source;

            if (price != null) {
                source = "ALPHA_VANTAGE";
            } else {
                // Fallback: simulate price movement ±0.3%
                price = simulatePrice(symbol);
                source = "SIMULATED";
            }

            if (price != null) {
                updateStockInDb(symbol, price);
                recordPriceHistory(AssetType.STOCK, symbol, price, source);
                updated++;
            }

            // Rate limiting: ~12 sec between calls = 5/min (Alpha Vantage free tier)
            if (!"demo".equals(apiKey)) {
                try {
                    Thread.sleep(12_000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    log.warn("Price update thread interrupted");
                    break;
                }
            }
        }

        log.info("Price update complete. Updated {}/{} symbols", updated, symbols.size());
    }

    /**
     * Updates a single stock's price in the database.
     * Falls back to simulation if API fails. Records price history.
     */
    public void updateSingleStockPrice(String symbol) {
        BigDecimal price = fetchCurrentPrice(symbol);
        String source;

        if (price != null) {
            source = "ALPHA_VANTAGE";
        } else {
            price = simulatePrice(symbol);
            source = "SIMULATED";
        }

        if (price != null) {
            updateStockInDb(symbol, price);
            recordPriceHistory(AssetType.STOCK, symbol, price, source);
        }
    }

    /**
     * Records a price history entry. Public so other services can use it.
     */
    public void recordPriceHistory(AssetType assetType, String symbol, BigDecimal price, String source) {
        try {
            AssetPriceHistory history = AssetPriceHistory.builder()
                    .assetType(assetType)
                    .symbol(symbol)
                    .price(price)
                    .recordedAt(LocalDateTime.now())
                    .source(source)
                    .build();
            priceHistoryRepository.save(history);
        } catch (Exception e) {
            log.warn("Failed to record price history for {}: {}", symbol, e.getMessage());
        }
    }

    // ===== Private Helpers =====

    /**
     * Simulates a price movement of ±0.3% based on the last known price.
     */
    private BigDecimal simulatePrice(String symbol) {
        return stockRepository.findById(symbol)
                .filter(s -> s.getCurrentPrice() != null)
                .map(stock -> {
                    double change = ThreadLocalRandom.current().nextDouble(-0.003, 0.003);
                    BigDecimal newPrice = stock.getCurrentPrice()
                            .multiply(BigDecimal.ONE.add(BigDecimal.valueOf(change)))
                            .setScale(4, RoundingMode.HALF_UP);
                    log.debug("Simulated price for {}: {} -> {} (change: {:.4f}%)",
                            symbol, stock.getCurrentPrice(), newPrice, change * 100);
                    return newPrice;
                })
                .orElse(null);
    }

    private void updateStockInDb(String symbol, BigDecimal price) {
        stockRepository.findById(symbol).ifPresentOrElse(
                stock -> {
                    stock.setCurrentPrice(price);
                    stock.setLastUpdated(LocalDateTime.now());
                    stockRepository.save(stock);
                },
                () -> {
                    // Auto-create a minimal stock entry
                    Stock newStock = Stock.builder()
                            .symbol(symbol)
                            .name(symbol)
                            .currentPrice(price)
                            .lastUpdated(LocalDateTime.now())
                            .build();
                    stockRepository.save(newStock);
                }
        );
    }
}
