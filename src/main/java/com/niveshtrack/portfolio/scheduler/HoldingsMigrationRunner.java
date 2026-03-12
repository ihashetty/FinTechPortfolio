package com.niveshtrack.portfolio.scheduler;

import com.niveshtrack.portfolio.entity.*;
import com.niveshtrack.portfolio.repository.HoldingRepository;
import com.niveshtrack.portfolio.repository.TransactionRepository;
import com.niveshtrack.portfolio.repository.UserRepository;
import com.niveshtrack.portfolio.util.FinancialUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * One-time migration runner that backfills the holdings table from existing
 * transactions for users who don't yet have persisted holdings.
 *
 * <p>Runs after DataSeeder (Order 2) so demo data is already present.
 * Safe to run multiple times — skips users who already have holdings.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class HoldingsMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final HoldingRepository holdingRepository;

    @Override
    @Transactional
    public void run(String... args) {
        List<User> users = userRepository.findAll();
        int migrated = 0;

        for (User user : users) {
            if (!holdingRepository.findByUserId(user.getId()).isEmpty()) {
                log.debug("User {} already has holdings — skipping migration", user.getEmail());
                continue;
            }

            List<Transaction> txns = transactionRepository
                    .findByUserIdOrderByTransactionDateDesc(user.getId());

            if (txns.isEmpty()) continue;

            // Group by symbol
            Map<String, List<Transaction>> grouped = txns.stream()
                    .collect(Collectors.groupingBy(Transaction::getStockSymbol));

            for (Map.Entry<String, List<Transaction>> entry : grouped.entrySet()) {
                String symbol = entry.getKey();
                List<Transaction> symbolTxns = entry.getValue();

                BigDecimal netQty = symbolTxns.stream()
                        .map(t -> t.getType() == TransactionType.BUY
                                ? t.getQuantity()
                                : t.getQuantity().negate())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                if (netQty.compareTo(BigDecimal.ZERO) <= 0) continue;

                BigDecimal avgPrice = FinancialUtils.calculateWeightedAverage(symbolTxns);

                // Determine asset type from transaction
                AssetType assetType = (symbolTxns.get(0).getAssetType() != null)
                        ? symbolTxns.get(0).getAssetType()
                        : AssetType.STOCK;

                Holding holding = Holding.builder()
                        .user(user)
                        .assetType(assetType)
                        .symbol(symbol)
                        .quantity(netQty.setScale(4, RoundingMode.HALF_UP))
                        .averagePrice(avgPrice.setScale(4, RoundingMode.HALF_UP))
                        .lastUpdated(LocalDateTime.now())
                        .build();

                holdingRepository.save(holding);
                migrated++;
            }

            log.info("Migrated holdings for user {} from transactions", user.getEmail());
        }

        if (migrated > 0) {
            log.info("Holdings migration complete: {} holdings created", migrated);
        } else {
            log.debug("No holdings needed migration.");
        }
    }
}
