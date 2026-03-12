package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.response.*;
import com.niveshtrack.portfolio.entity.AssetType;
import com.niveshtrack.portfolio.entity.PortfolioSnapshot;
import com.niveshtrack.portfolio.entity.Transaction;
import com.niveshtrack.portfolio.entity.TransactionType;
import com.niveshtrack.portfolio.repository.SipInstructionRepository;
import com.niveshtrack.portfolio.repository.SnapshotRepository;
import com.niveshtrack.portfolio.repository.TransactionRepository;
import com.niveshtrack.portfolio.util.DateUtils;
import com.niveshtrack.portfolio.util.XIRRCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Portfolio-level analytics: dashboard summary, allocation, and growth charts.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioService {

    private final HoldingsService holdingsService;
    private final SnapshotRepository snapshotRepository;
    private final TransactionRepository transactionRepository;
    private final SipInstructionRepository sipInstructionRepository;

    // ===== Dashboard Summary =====

    /**
     * Builds the main dashboard summary card.
     */
    @Transactional(readOnly = true)
    public DashboardSummaryDTO getDashboardSummary(Long userId) {
        List<HoldingDTO> holdings = holdingsService.calculateHoldings(userId);

        BigDecimal totalInvested = holdings.stream()
                .map(HoldingDTO::getInvestedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentValue = holdings.stream()
                .map(HoldingDTO::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPL = currentValue.subtract(totalInvested);
        BigDecimal returnPercent = totalInvested.compareTo(BigDecimal.ZERO) > 0
                ? totalPL.divide(totalInvested, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        HoldingDTO topGainer = holdings.stream()
                .max(Comparator.comparing(HoldingDTO::getReturnPercent))
                .orElse(null);

        HoldingDTO topLoser = holdings.stream()
                .min(Comparator.comparing(HoldingDTO::getReturnPercent))
                .orElse(null);

        HoldingDTO largestHolding = holdings.stream()
                .max(Comparator.comparing(HoldingDTO::getTotalValue))
                .orElse(null);

        // XIRR calculation
        BigDecimal xirr = calculateXirr(userId, holdings);

        long totalTransactions = transactionRepository.countByUserId(userId);

        // SIP summary
        var allSips = sipInstructionRepository.findByUserIdOrderByCreatedAtDesc(userId);
        int activeSipCount = (int) allSips.stream().filter(s -> Boolean.TRUE.equals(s.getActive())).count();
        BigDecimal monthlySipTotal = allSips.stream()
                .filter(s -> Boolean.TRUE.equals(s.getActive()))
                .map(s -> s.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return DashboardSummaryDTO.builder()
                .totalInvested(totalInvested.setScale(2, RoundingMode.HALF_UP))
                .currentValue(currentValue.setScale(2, RoundingMode.HALF_UP))
                .totalPL(totalPL.setScale(2, RoundingMode.HALF_UP))
                .returnPercent(returnPercent)
                .xirr(xirr)
                .totalHoldings(holdings.size())
                .totalTransactions(totalTransactions)
                .topGainer(topGainer)
                .topLoser(topLoser)
                .largestHolding(largestHolding)
                .activeSipCount(activeSipCount)
                .monthlySipTotal(monthlySipTotal.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    // ===== Portfolio Allocation =====

    /**
     * Groups current holdings by sector and computes allocation percentages.
     */
    @Transactional(readOnly = true)
    public List<PortfolioAllocationDTO> getPortfolioAllocation(Long userId) {
        List<HoldingDTO> holdings = holdingsService.calculateHoldings(userId);

        BigDecimal totalValue = holdings.stream()
                .map(HoldingDTO::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, List<HoldingDTO>> bySector = holdings.stream()
                .collect(Collectors.groupingBy(h ->
                        h.getSector() != null ? h.getSector() : "Others"));

        return bySector.entrySet().stream()
                .map(entry -> {
                    BigDecimal sectorValue = entry.getValue().stream()
                            .map(HoldingDTO::getTotalValue)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    BigDecimal percentage = totalValue.compareTo(BigDecimal.ZERO) > 0
                            ? sectorValue.divide(totalValue, 4, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(100))
                                    .setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;

                    return PortfolioAllocationDTO.builder()
                            .sector(entry.getKey())
                            .totalValue(sectorValue.setScale(2, RoundingMode.HALF_UP))
                            .percentage(percentage)
                            .stockCount(entry.getValue().size())
                            .build();
                })
                .sorted(Comparator.comparing(PortfolioAllocationDTO::getTotalValue).reversed())
                .collect(Collectors.toList());
    }

    // ===== Portfolio Growth =====

    /**
     * Returns the last 13 months of portfolio value snapshots for the growth chart.
     * Falls back to computing from transactions if snapshots are not available.
     */
    @Transactional(readOnly = true)
    public List<PortfolioGrowthDTO> getPortfolioGrowth(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(13);

        List<PortfolioSnapshot> snapshots =
                snapshotRepository.findByUserIdAndSnapshotDateBetweenOrderBySnapshotDateAsc(
                        userId, startDate, endDate);

        if (!snapshots.isEmpty()) {
            return snapshots.stream()
                    .map(s -> PortfolioGrowthDTO.builder()
                            .date(s.getSnapshotDate())
                            .totalValue(s.getTotalValue())
                            .totalInvested(s.getTotalInvested())
                            .pnl(s.getTotalValue() != null && s.getTotalInvested() != null
                                    ? s.getTotalValue().subtract(s.getTotalInvested())
                                    : BigDecimal.ZERO)
                            .monthLabel(DateUtils.toMonthLabel(s.getSnapshotDate()))
                            .build())
                    .collect(Collectors.toList());
        }

        // Fallback: compute approximate monthly values from transaction history
        return computeGrowthFromTransactions(userId, startDate, endDate);
    }

    // ===== Investment Split (SIP vs Lumpsum) =====

    /**
     * Calculates the breakdown of mutual fund investments by mode: SIP vs Lumpsum.
     * SIP transactions are identified by notes containing "SIP".
     */
    @Transactional(readOnly = true)
    public InvestmentSplitDTO getInvestmentSplit(Long userId) {
        List<Transaction> mfBuys = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId)
                .stream()
                .filter(t -> t.getAssetType() == AssetType.MF && t.getType() == TransactionType.BUY)
                .collect(Collectors.toList());

        BigDecimal sipAmount = BigDecimal.ZERO;
        BigDecimal lumpsumAmount = BigDecimal.ZERO;

        for (Transaction t : mfBuys) {
            BigDecimal amount = t.getPrice().multiply(t.getQuantity());
            String notes = t.getNotes() != null ? t.getNotes().toLowerCase() : "";
            if (notes.contains("sip")) {
                sipAmount = sipAmount.add(amount);
            } else {
                lumpsumAmount = lumpsumAmount.add(amount);
            }
        }

        BigDecimal total = sipAmount.add(lumpsumAmount);
        BigDecimal sipPercent = total.compareTo(BigDecimal.ZERO) > 0
                ? sipAmount.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        BigDecimal lumpsumPercent = total.compareTo(BigDecimal.ZERO) > 0
                ? lumpsumAmount.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return InvestmentSplitDTO.builder()
                .sipAmount(sipAmount.setScale(2, RoundingMode.HALF_UP))
                .lumpsumAmount(lumpsumAmount.setScale(2, RoundingMode.HALF_UP))
                .sipPercent(sipPercent)
                .lumpsumPercent(lumpsumPercent)
                .build();
    }

    // ===== Private Helpers =====

    private BigDecimal calculateXirr(Long userId, List<HoldingDTO> holdings) {
        try {
            List<Transaction> transactions =
                    transactionRepository.findByUserIdOrderByTransactionDateDesc(userId);

            if (transactions.isEmpty()) return null;

            List<XIRRCalculator.CashFlow> cashFlows = new ArrayList<>();

            // Outflows: BUY transactions (negative)
            transactions.stream()
                    .filter(t -> t.getType() == TransactionType.BUY)
                    .forEach(t -> {
                        double amount = t.getPrice().doubleValue() * t.getQuantity().doubleValue()
                                + (t.getBrokerage() != null ? t.getBrokerage().doubleValue() : 0);
                        cashFlows.add(new XIRRCalculator.CashFlow(t.getTransactionDate(), -amount));
                    });

            // Inflows: SELL transactions (positive)
            transactions.stream()
                    .filter(t -> t.getType() == TransactionType.SELL)
                    .forEach(t -> {
                        double amount = t.getPrice().doubleValue() * t.getQuantity().doubleValue()
                                - (t.getBrokerage() != null ? t.getBrokerage().doubleValue() : 0);
                        cashFlows.add(new XIRRCalculator.CashFlow(t.getTransactionDate(), amount));
                    });

            // Terminal inflow: current portfolio value today
            double currentValue = holdings.stream()
                    .mapToDouble(h -> h.getTotalValue().doubleValue())
                    .sum();

            if (currentValue > 0) {
                cashFlows.add(new XIRRCalculator.CashFlow(LocalDate.now(), currentValue));
            }

            // Sort by date
            cashFlows.sort(Comparator.comparing(XIRRCalculator.CashFlow::date));

            return XIRRCalculator.calculate(cashFlows);
        } catch (Exception e) {
            log.warn("XIRR calculation failed for userId={}: {}", userId, e.getMessage());
            return null;
        }
    }

    private List<PortfolioGrowthDTO> computeGrowthFromTransactions(
            Long userId, LocalDate startDate, LocalDate endDate) {

        List<Transaction> allTxns = transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateAsc(userId, startDate, endDate);

        // Simple cumulative invested amount as proxy
        List<PortfolioGrowthDTO> result = new ArrayList<>();
        BigDecimal cumInvested = BigDecimal.ZERO;

        Map<String, List<Transaction>> byMonth = allTxns.stream()
                .collect(Collectors.groupingBy(t -> DateUtils.toMonthKey(t.getTransactionDate())));

        LocalDate cursor = startDate.withDayOfMonth(1);
        while (!cursor.isAfter(endDate)) {
            String key = DateUtils.toMonthKey(cursor);
            List<Transaction> monthTxns = byMonth.getOrDefault(key, List.of());

            for (Transaction t : monthTxns) {
                BigDecimal amount = t.getPrice().multiply(t.getQuantity());
                if (t.getType() == TransactionType.BUY) {
                    cumInvested = cumInvested.add(amount);
                } else {
                    cumInvested = cumInvested.subtract(amount);
                }
            }

            result.add(PortfolioGrowthDTO.builder()
                    .date(cursor)
                    .totalValue(cumInvested)
                    .totalInvested(cumInvested)
                    .pnl(BigDecimal.ZERO)
                    .monthLabel(DateUtils.toMonthLabel(cursor))
                    .build());

            cursor = cursor.plusMonths(1);
        }

        return result;
    }
}
