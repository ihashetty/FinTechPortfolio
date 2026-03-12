package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.request.AddWatchlistRequest;
import com.niveshtrack.portfolio.dto.response.WatchlistItemDTO;
import com.niveshtrack.portfolio.entity.Stock;
import com.niveshtrack.portfolio.entity.User;
import com.niveshtrack.portfolio.entity.Watchlist;
import com.niveshtrack.portfolio.exception.DuplicateResourceException;
import com.niveshtrack.portfolio.exception.ResourceNotFoundException;
import com.niveshtrack.portfolio.repository.StockRepository;
import com.niveshtrack.portfolio.repository.UserRepository;
import com.niveshtrack.portfolio.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages user watchlists (add, remove, retrieve with current prices).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    /**
     * Returns all watchlist items for a user enriched with current price data.
     */
    @Transactional(readOnly = true)
    public List<WatchlistItemDTO> getWatchlist(Long userId) {
        return watchlistRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::enrichWithPrice)
                .collect(Collectors.toList());
    }

    /**
     * Adds a stock to the user's watchlist. Throws {@link DuplicateResourceException} if already present.
     */
    @Transactional
    public WatchlistItemDTO addToWatchlist(Long userId, AddWatchlistRequest request) {
        if (watchlistRepository.existsByUserIdAndStockSymbol(userId, request.getStockSymbol().toUpperCase())) {
            throw new DuplicateResourceException("Watchlist item", "symbol", request.getStockSymbol());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Try to resolve the stock name from the stocks table if not provided
        String stockName = request.getStockName();
        if (stockName == null || stockName.isBlank()) {
            stockName = stockRepository.findById(request.getStockSymbol().toUpperCase())
                    .map(Stock::getName)
                    .orElse(request.getStockSymbol());
        }

        Watchlist entry = Watchlist.builder()
                .user(user)
                .stockSymbol(request.getStockSymbol().toUpperCase())
                .stockName(stockName)
                .addedDate(LocalDate.now())
                .build();

        Watchlist saved = watchlistRepository.save(entry);
        log.info("Added to watchlist: userId={}, symbol={}", userId, saved.getStockSymbol());
        return enrichWithPrice(saved);
    }

    /**
     * Removes a stock from the watchlist. Verifies ownership.
     */
    @Transactional
    public void removeFromWatchlist(Long userId, Long watchlistId) {
        Watchlist entry = watchlistRepository.findByIdAndUserId(watchlistId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Watchlist item", "id", watchlistId));

        watchlistRepository.delete(entry);
        log.info("Removed from watchlist: userId={}, symbol={}", userId, entry.getStockSymbol());
    }

    // ===== Helper =====

    private WatchlistItemDTO enrichWithPrice(Watchlist w) {
        Stock stock = stockRepository.findById(w.getStockSymbol()).orElse(null);

        return WatchlistItemDTO.builder()
                .id(w.getId())
                .stockSymbol(w.getStockSymbol())
                .stockName(w.getStockName())
                .sector(stock != null ? stock.getSector() : null)
                .addedDate(w.getAddedDate())
                .currentPrice(stock != null ? stock.getCurrentPrice() : null)
                .dayChange(null)         // requires intraday feed
                .dayChangePercent(null)
                .createdAt(w.getCreatedAt())
                .build();
    }
}
