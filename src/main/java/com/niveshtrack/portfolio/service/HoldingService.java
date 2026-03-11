package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.response.HoldingDTO;
import com.niveshtrack.portfolio.entity.*;
import com.niveshtrack.portfolio.exception.ResourceNotFoundException;
import com.niveshtrack.portfolio.exception.ValidationException;
import com.niveshtrack.portfolio.repository.HoldingRepository;
import com.niveshtrack.portfolio.repository.MutualFundRepository;
import com.niveshtrack.portfolio.repository.StockRepository;
import com.niveshtrack.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing persisted holdings in the holdings table.
 *
 * <p>Updates holdings after every BUY/SELL using the weighted average price formula.
 * Uses optimistic locking ({@code @Version}) to handle concurrent modifications.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final StockRepository stockRepository;
    private final MutualFundRepository mutualFundRepository;
    private final UserRepository userRepository;

    // ===== Write Operations =====

    /**
     * Updates or creates a holding after a BUY transaction.
     *
     * <p>Weighted average formula:
     * {@code newAvg = ((oldQty × oldAvg) + (newQty × newPrice)) / (oldQty + newQty)}
     */
    @Transactional
    public void updateHoldingAfterBuy(Long userId, AssetType assetType, String symbol,
                                       BigDecimal quantity, BigDecimal price) {
        Optional<Holding> existing = holdingRepository
                .findByUserIdAndAssetTypeAndSymbol(userId, assetType, symbol);

        if (existing.isPresent()) {
            Holding holding = existing.get();
            BigDecimal oldQty = holding.getQuantity();
            BigDecimal oldAvg = holding.getAveragePrice();

            BigDecimal newTotalCost = oldQty.multiply(oldAvg).add(quantity.multiply(price));
            BigDecimal newTotalQty = oldQty.add(quantity);
            BigDecimal newAvg = newTotalCost.divide(newTotalQty, 4, RoundingMode.HALF_UP);

            holding.setQuantity(newTotalQty);
            holding.setAveragePrice(newAvg);
            holding.setLastUpdated(LocalDateTime.now());
            holdingRepository.save(holding);

            log.info("Holding updated (BUY): userId={}, asset={}, symbol={}, qty={}, avg={}",
                    userId, assetType, symbol, newTotalQty, newAvg);
        } else {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

            Holding newHolding = Holding.builder()
                    .user(user)
                    .assetType(assetType)
                    .symbol(symbol)
                    .quantity(quantity)
                    .averagePrice(price)
                    .lastUpdated(LocalDateTime.now())
                    .build();
            holdingRepository.save(newHolding);

            log.info("Holding created (BUY): userId={}, asset={}, symbol={}, qty={}, price={}",
                    userId, assetType, symbol, quantity, price);
        }
    }

    /**
     * Updates a holding after a SELL transaction.
     * If quantity reaches zero, deletes the holding row.
     *
     * @throws ValidationException if sell quantity exceeds held quantity
     */
    @Transactional
    public void updateHoldingAfterSell(Long userId, AssetType assetType, String symbol,
                                        BigDecimal sellQuantity) {
        Holding holding = holdingRepository
                .findByUserIdAndAssetTypeAndSymbol(userId, assetType, symbol)
                .orElseThrow(() -> new ValidationException(
                        String.format("No holding found for %s %s", assetType, symbol)));

        if (sellQuantity.compareTo(holding.getQuantity()) > 0) {
            throw new ValidationException(
                    String.format("Cannot sell %s units of %s — only %s held.",
                            sellQuantity.toPlainString(), symbol,
                            holding.getQuantity().toPlainString()));
        }

        BigDecimal newQty = holding.getQuantity().subtract(sellQuantity);

        if (newQty.compareTo(BigDecimal.ZERO) == 0) {
            holdingRepository.delete(holding);
            log.info("Holding deleted (fully sold): userId={}, asset={}, symbol={}",
                    userId, assetType, symbol);
        } else {
            holding.setQuantity(newQty);
            // Average price stays the same on SELL
            holding.setLastUpdated(LocalDateTime.now());
            holdingRepository.save(holding);
            log.info("Holding updated (SELL): userId={}, asset={}, symbol={}, remainingQty={}",
                    userId, assetType, symbol, newQty);
        }
    }

    // ===== Read Operations =====

    /**
     * Returns all holdings for a user, enriched with current market prices.
     */
    @Transactional(readOnly = true)
    public List<HoldingDTO> getUserHoldings(Long userId) {
        List<Holding> holdings = holdingRepository.findByUserId(userId);
        return enrichHoldings(holdings);
    }

    /**
     * Returns holdings filtered by asset type.
     */
    @Transactional(readOnly = true)
    public List<HoldingDTO> getUserHoldingsByAssetType(Long userId, AssetType assetType) {
        List<Holding> holdings = holdingRepository.findByUserIdAndAssetType(userId, assetType);
        return enrichHoldings(holdings);
    }

    /**
     * Checks if user has sufficient quantity of an asset for selling.
     */
    @Transactional(readOnly = true)
    public BigDecimal getHeldQuantity(Long userId, AssetType assetType, String symbol) {
        return holdingRepository.findByUserIdAndAssetTypeAndSymbol(userId, assetType, symbol)
                .map(Holding::getQuantity)
                .orElse(BigDecimal.ZERO);
    }

    // ===== Helpers =====

    private List<HoldingDTO> enrichHoldings(List<Holding> holdings) {
        List<HoldingDTO> result = new ArrayList<>();
        BigDecimal totalPortfolioValue = BigDecimal.ZERO;

        // First pass: compute values
        for (Holding h : holdings) {
            BigDecimal currentPrice = getCurrentPrice(h.getAssetType(), h.getSymbol());
            BigDecimal investedAmount = h.getAveragePrice().multiply(h.getQuantity());
            BigDecimal totalValue = currentPrice.multiply(h.getQuantity());
            BigDecimal gainLoss = totalValue.subtract(investedAmount);
            BigDecimal returnPercent = investedAmount.compareTo(BigDecimal.ZERO) > 0
                    ? gainLoss.divide(investedAmount, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            String name = getAssetName(h.getAssetType(), h.getSymbol());
            String sector = getAssetSector(h.getAssetType(), h.getSymbol());

            HoldingDTO dto = HoldingDTO.builder()
                    .assetType(h.getAssetType().name())
                    .symbol(h.getSymbol())
                    .name(name)
                    .sector(sector)
                    .quantity(h.getQuantity().setScale(4, RoundingMode.HALF_UP))
                    .avgBuyPrice(h.getAveragePrice().setScale(2, RoundingMode.HALF_UP))
                    .investedAmount(investedAmount.setScale(2, RoundingMode.HALF_UP))
                    .currentPrice(currentPrice.setScale(2, RoundingMode.HALF_UP))
                    .totalValue(totalValue.setScale(2, RoundingMode.HALF_UP))
                    .gainLoss(gainLoss.setScale(2, RoundingMode.HALF_UP))
                    .returnPercent(returnPercent)
                    .dayChange(BigDecimal.ZERO)
                    .weightPercent(BigDecimal.ZERO)
                    .build();

            result.add(dto);
            totalPortfolioValue = totalPortfolioValue.add(totalValue);
        }

        // Second pass: compute weight percentages
        if (totalPortfolioValue.compareTo(BigDecimal.ZERO) > 0) {
            for (HoldingDTO dto : result) {
                BigDecimal weight = dto.getTotalValue()
                        .divide(totalPortfolioValue, 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                dto.setWeightPercent(weight);
            }
        }

        // Sort by invested amount descending
        result.sort((a, b) -> b.getInvestedAmount().compareTo(a.getInvestedAmount()));
        return result;
    }

    private BigDecimal getCurrentPrice(AssetType assetType, String symbol) {
        if (assetType == AssetType.STOCK) {
            return stockRepository.findById(symbol)
                    .map(Stock::getCurrentPrice)
                    .orElse(BigDecimal.ZERO);
        } else {
            return mutualFundRepository.findBySymbol(symbol)
                    .map(MutualFund::getNav)
                    .orElse(BigDecimal.ZERO);
        }
    }

    private String getAssetName(AssetType assetType, String symbol) {
        if (assetType == AssetType.STOCK) {
            return stockRepository.findById(symbol)
                    .map(Stock::getName)
                    .orElse(symbol);
        } else {
            return mutualFundRepository.findBySymbol(symbol)
                    .map(MutualFund::getName)
                    .orElse(symbol);
        }
    }

    private String getAssetSector(AssetType assetType, String symbol) {
        if (assetType == AssetType.STOCK) {
            return stockRepository.findById(symbol)
                    .map(Stock::getSector)
                    .orElse("Unknown");
        } else {
            return mutualFundRepository.findBySymbol(symbol)
                    .map(MutualFund::getCategory)
                    .orElse("Mutual Fund");
        }
    }
}
