package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.response.StockDTO;
import com.niveshtrack.portfolio.entity.Stock;
import com.niveshtrack.portfolio.exception.ResourceNotFoundException;
import com.niveshtrack.portfolio.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for stock master data — search, lookup, and price retrieval.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final StockRepository stockRepository;

    /**
     * Search stocks by symbol or name (case-insensitive partial match).
     * Results are capped at {@code limit} for fast dropdown rendering.
     *
     * @param query  search term (matched against both symbol and name)
     * @param limit  maximum results to return
     * @return matching stocks
     */
    @Transactional(readOnly = true)
    public List<StockDTO> searchStocks(String query, int limit) {
        List<Stock> results = stockRepository
                .findBySymbolContainingIgnoreCaseOrNameContainingIgnoreCase(query, query);

        return results.stream()
                .limit(limit)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Returns a single stock by its symbol (primary key).
     *
     * @param symbol NSE ticker, e.g. "RELIANCE"
     * @return stock DTO
     * @throws ResourceNotFoundException if symbol not found
     */
    @Transactional(readOnly = true)
    public StockDTO getStock(String symbol) {
        Stock stock = stockRepository.findById(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Stock", "symbol", symbol));
        return toDTO(stock);
    }

    /**
     * Returns the current market price for a stock symbol.
     *
     * @param symbol NSE ticker
     * @return current price, or null if not found
     */
    @Transactional(readOnly = true)
    public BigDecimal getCurrentPrice(String symbol) {
        return stockRepository.findById(symbol.toUpperCase())
                .map(Stock::getCurrentPrice)
                .orElse(null);
    }

    /**
     * Returns all stocks in the database.
     */
    @Transactional(readOnly = true)
    public List<StockDTO> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ===== Helpers =====

    private StockDTO toDTO(Stock stock) {
        return StockDTO.builder()
                .symbol(stock.getSymbol())
                .name(stock.getName())
                .sector(stock.getSector())
                .currentPrice(stock.getCurrentPrice())
                .lastUpdated(stock.getLastUpdated())
                .build();
    }
}
