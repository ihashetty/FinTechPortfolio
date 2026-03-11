package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.request.CreateAlertRequest;
import com.niveshtrack.portfolio.dto.response.PriceAlertDTO;
import com.niveshtrack.portfolio.entity.PriceAlert;
import com.niveshtrack.portfolio.entity.Stock;
import com.niveshtrack.portfolio.entity.User;
import com.niveshtrack.portfolio.exception.ResourceNotFoundException;
import com.niveshtrack.portfolio.repository.AlertRepository;
import com.niveshtrack.portfolio.repository.StockRepository;
import com.niveshtrack.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages price alert creation, retrieval, and deletion.
 * The actual alert checking is handled by {@code AlertCheckerScheduler}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;
    private final StockRepository stockRepository;

    /**
     * Returns all alerts for a user (both active and triggered).
     */
    @Transactional(readOnly = true)
    public List<PriceAlertDTO> getAlerts(Long userId) {
        return alertRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creates a new price alert.
     */
    @Transactional
    public PriceAlertDTO createAlert(Long userId, CreateAlertRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        String stockName = request.getStockName();
        if (stockName == null || stockName.isBlank()) {
            stockName = stockRepository.findById(request.getStockSymbol().toUpperCase())
                    .map(Stock::getName)
                    .orElse(request.getStockSymbol());
        }

        PriceAlert alert = PriceAlert.builder()
                .user(user)
                .stockSymbol(request.getStockSymbol().toUpperCase())
                .stockName(stockName)
                .targetPrice(request.getTargetPrice())
                .direction(request.getDirection())
                .active(true)
                .build();

        PriceAlert saved = alertRepository.save(alert);
        log.info("Alert created: userId={}, symbol={}, direction={}, target={}",
                userId, saved.getStockSymbol(), saved.getDirection(), saved.getTargetPrice());
        return toDTO(saved);
    }

    /**
     * Deletes an alert. Verifies the caller owns it.
     */
    @Transactional
    public void deleteAlert(Long userId, Long alertId) {
        PriceAlert alert = alertRepository.findByIdAndUserId(alertId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", "id", alertId));

        alertRepository.delete(alert);
        log.info("Alert deleted: id={}, userId={}", alertId, userId);
    }

    // ===== Helper =====

    private PriceAlertDTO toDTO(PriceAlert a) {
        Stock stock = stockRepository.findById(a.getStockSymbol()).orElse(null);

        return PriceAlertDTO.builder()
                .id(a.getId())
                .stockSymbol(a.getStockSymbol())
                .stockName(a.getStockName())
                .targetPrice(a.getTargetPrice())
                .direction(a.getDirection())
                .active(a.getActive())
                .currentPrice(stock != null ? stock.getCurrentPrice() : null)
                .triggeredAt(a.getTriggeredAt())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
