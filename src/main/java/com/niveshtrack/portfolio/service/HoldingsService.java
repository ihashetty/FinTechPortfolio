package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.response.HoldingDTO;
import com.niveshtrack.portfolio.entity.Stock;
import com.niveshtrack.portfolio.entity.Transaction;
import com.niveshtrack.portfolio.entity.TransactionType;
import com.niveshtrack.portfolio.repository.HoldingRepository;
import com.niveshtrack.portfolio.repository.StockRepository;
import com.niveshtrack.portfolio.repository.TransactionRepository;
import com.niveshtrack.portfolio.util.FinancialUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Core calculation engine for portfolio holdings.
 *
 * <p>Bridge service: first checks the persisted holdings table.
 * If the holdings table has data for the user, uses it (via HoldingService).
 * Falls back to computing from transactions for backward compatibility.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingsService {

    private final TransactionRepository transactionRepository;
    private final StockRepository stockRepository;
    private final HoldingRepository holdingRepository;
    private final HoldingService holdingService;

    /**
     * Returns current portfolio holdings for a user.
     *
     * <p>Strategy:
     * <ol>
     *   <li>Check if persisted holdings exist → use HoldingService</li>
     *   <li>Fallback: compute from transactions (legacy path)</li>
     * </ol>
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "holdings", key = "#userId")
    public List<HoldingDTO> calculateHoldings(Long userId) {
        // Check persisted holdings table first
        if (!holdingRepository.findByUserId(userId).isEmpty()) {
            log.debug("Using persisted holdings for userId={}", userId);
            return holdingService.getUserHoldings(userId);
        }

        // Fallback: compute from transactions (legacy)
        log.debug("Computing holdings from transactions for userId={}", userId);
        return computeHoldingsFromTransactions(userId);
    }

    /** Evicts the holdings cache when transactions change */
    @CacheEvict(value = "holdings", key = "#userId")
    public void evictHoldingsCache(Long userId) {
        log.debug("Evicted holdings cache for userId={}", userId);
    }

    // ===== Legacy computed holdings (fallback) =====

    private List<HoldingDTO> computeHoldingsFromTransactions(Long userId) {
        List<Transaction> allTransactions =
                transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);

        if (allTransactions.isEmpty()) {
            return new ArrayList<>();
        }

        // Group transactions by stock symbol
        Map<String, List<Transaction>> grouped = allTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::getStockSymbol));

        List<HoldingDTO> holdings = new ArrayList<>();

        for (Map.Entry<String, List<Transaction>> entry : grouped.entrySet()) {
            String symbol = entry.getKey();
            List<Transaction> txns = entry.getValue();

            // Net quantity: BUY adds, SELL subtracts
            BigDecimal netQty = txns.stream()
                    .map(t -> t.getType() == TransactionType.BUY
                            ? t.getQuantity()
                            : t.getQuantity().negate())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (netQty.compareTo(BigDecimal.ZERO) <= 0) continue;

            // Weighted average buy price (only BUY transactions)
            BigDecimal avgBuyPrice = FinancialUtils.calculateWeightedAverage(txns);

            // Fetch current market price
            Stock stock = stockRepository.findById(symbol).orElse(null);
            BigDecimal currentPrice = (stock != null && stock.getCurrentPrice() != null)
                    ? stock.getCurrentPrice()
                    : BigDecimal.ZERO;

            // Financial metrics
            BigDecimal investedAmount = avgBuyPrice.multiply(netQty);
            BigDecimal totalValue = currentPrice.multiply(netQty);
            BigDecimal gainLoss = totalValue.subtract(investedAmount);
            BigDecimal returnPercent = FinancialUtils.calculateReturnPercent(gainLoss, investedAmount);

            HoldingDTO holding = HoldingDTO.builder()
                    .assetType("STOCK")
                    .symbol(symbol)
                    .name(stock != null ? stock.getName() : symbol)
                    .sector(stock != null ? stock.getSector() : "Unknown")
                    .quantity(netQty.setScale(4, RoundingMode.HALF_UP))
                    .avgBuyPrice(avgBuyPrice.setScale(2, RoundingMode.HALF_UP))
                    .investedAmount(investedAmount.setScale(2, RoundingMode.HALF_UP))
                    .currentPrice(currentPrice.setScale(2, RoundingMode.HALF_UP))
                    .totalValue(totalValue.setScale(2, RoundingMode.HALF_UP))
                    .gainLoss(gainLoss.setScale(2, RoundingMode.HALF_UP))
                    .returnPercent(returnPercent)
                    .dayChange(BigDecimal.ZERO)
                    .weightPercent(BigDecimal.ZERO)
                    .build();

            holdings.add(holding);
        }

        // Calculate weight percentages
        BigDecimal totalPortfolioValue = holdings.stream()
                .map(HoldingDTO::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalPortfolioValue.compareTo(BigDecimal.ZERO) > 0) {
            holdings.forEach(h -> {
                BigDecimal weight = h.getTotalValue()
                        .divide(totalPortfolioValue, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                h.setWeightPercent(weight);
            });
        }

        // Sort by invested amount descending
        holdings.sort((a, b) -> b.getInvestedAmount().compareTo(a.getInvestedAmount()));

        log.debug("Calculated {} holdings for userId={}", holdings.size(), userId);
        return holdings;
    }
}
