package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.response.MonthlyPLDTO;
import com.niveshtrack.portfolio.dto.response.PortfolioAllocationDTO;
import com.niveshtrack.portfolio.dto.response.TaxSummaryDTO;
import com.niveshtrack.portfolio.entity.TaxType;
import com.niveshtrack.portfolio.entity.Transaction;
import com.niveshtrack.portfolio.entity.TransactionType;
import com.niveshtrack.portfolio.repository.TransactionRepository;
import com.niveshtrack.portfolio.util.DateUtils;
import com.niveshtrack.portfolio.util.FinancialUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics service: monthly P&L, tax summary, sector allocation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final PortfolioService portfolioService;

    /**
     * Calculates monthly realised P&L for the last 12 months.
     */
    @Transactional(readOnly = true)
    public List<MonthlyPLDTO> getMonthlyPL(Long userId) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(11).withDayOfMonth(1);

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetweenOrderByTransactionDateAsc(
                        userId, startDate, endDate);

        // Group by YYYY-MM key
        Map<String, List<Transaction>> byMonth = transactions.stream()
                .collect(Collectors.groupingBy(
                        t -> DateUtils.toMonthKey(t.getTransactionDate())));

        List<MonthlyPLDTO> result = new ArrayList<>();
        LocalDate cursor = startDate;

        while (!cursor.isAfter(endDate)) {
            String key = DateUtils.toMonthKey(cursor);
            List<Transaction> monthTxns = byMonth.getOrDefault(key, List.of());

            BigDecimal invested = monthTxns.stream()
                    .filter(t -> t.getType() == TransactionType.BUY)
                    .map(t -> t.getPrice().multiply(t.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal proceeds = monthTxns.stream()
                    .filter(t -> t.getType() == TransactionType.SELL)
                    .map(t -> t.getPrice().multiply(t.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal realisedPL = proceeds.subtract(invested);

            result.add(MonthlyPLDTO.builder()
                    .month(key)
                    .monthLabel(DateUtils.toMonthLabel(cursor))
                    .year(cursor.getYear())
                    .monthNumber(cursor.getMonthValue())
                    .realisedPL(realisedPL.setScale(2, RoundingMode.HALF_UP))
                    .unrealisedPL(BigDecimal.ZERO) // snapshot-based; simplified here
                    .invested(invested.setScale(2, RoundingMode.HALF_UP))
                    .proceeds(proceeds.setScale(2, RoundingMode.HALF_UP))
                    .build());

            cursor = cursor.plusMonths(1);
        }

        return result;
    }

    /**
     * Calculates STCG/LTCG tax summary for a given Indian financial year.
     *
     * <p>Uses FIFO matching: earliest BUY transactions are matched against SELL transactions.
     *
     * @param userId         the user
     * @param financialYear  e.g. "2024-25"
     */
    @Transactional(readOnly = true)
    public TaxSummaryDTO getTaxSummary(Long userId, String financialYear) {
        String fy = (financialYear != null && !financialYear.isBlank())
                ? financialYear
                : DateUtils.currentFinancialYear();

        LocalDate fyStart = DateUtils.fyStartDate(fy);
        LocalDate fyEnd = DateUtils.fyEndDate(fy);

        // All transactions up to FY end (need BUY history for FIFO matching)
        List<Transaction> allTxns = transactionRepository.findByUserIdOrderByTransactionDateDesc(userId)
                .stream()
                .filter(t -> !t.getTransactionDate().isAfter(fyEnd))
                .sorted(Comparator.comparing(Transaction::getTransactionDate))
                .collect(Collectors.toList());

        // Separate into BUY/SELL queues per symbol
        Map<String, Deque<Transaction>> buyQueues = new LinkedHashMap<>();
        Map<String, List<Transaction>> sellsBySymbol = new LinkedHashMap<>();

        for (Transaction t : allTxns) {
            String sym = t.getStockSymbol();
            if (t.getType() == TransactionType.BUY) {
                buyQueues.computeIfAbsent(sym, k -> new ArrayDeque<>()).add(t);
            } else if (DateUtils.isInFinancialYear(t.getTransactionDate(), fy)) {
                sellsBySymbol.computeIfAbsent(sym, k -> new ArrayList<>()).add(t);
            }
        }

        // FIFO matching
        List<TaxSummaryDTO.TaxLineItem> lineItems = new ArrayList<>();
        BigDecimal totalStcgGains = BigDecimal.ZERO;
        BigDecimal totalStcgLosses = BigDecimal.ZERO;
        BigDecimal totalLtcgGains = BigDecimal.ZERO;
        BigDecimal totalLtcgLosses = BigDecimal.ZERO;

        for (Map.Entry<String, List<Transaction>> entry : sellsBySymbol.entrySet()) {
            String sym = entry.getKey();
            Deque<Transaction> buyQueue = buyQueues.getOrDefault(sym, new ArrayDeque<>());

            for (Transaction sell : entry.getValue()) {
                BigDecimal remainingQty = sell.getQuantity();

                while (remainingQty.compareTo(BigDecimal.ZERO) > 0 && !buyQueue.isEmpty()) {
                    Transaction buy = buyQueue.peek();
                    BigDecimal matchQty = remainingQty.min(buy.getQuantity());

                    long holdingDays = FinancialUtils.calculateHoldingPeriodDays(
                            buy.getTransactionDate(), sell.getTransactionDate());
                    TaxType taxType = FinancialUtils.classifyTaxType(holdingDays);

                    BigDecimal gain = sell.getPrice().subtract(buy.getPrice())
                            .multiply(matchQty);

                    lineItems.add(TaxSummaryDTO.TaxLineItem.builder()
                            .stockSymbol(sym)
                            .stockName(sell.getStockName())
                            .quantity(matchQty.intValue())
                            .buyPrice(buy.getPrice())
                            .sellPrice(sell.getPrice())
                            .buyDate(buy.getTransactionDate().toString())
                            .sellDate(sell.getTransactionDate().toString())
                            .holdingDays((int) holdingDays)
                            .taxType(taxType.name())
                            .gain(gain.setScale(2, RoundingMode.HALF_UP))
                            .taxLiability(BigDecimal.ZERO) // computed after netting
                            .build());

                    if (taxType == TaxType.STCG) {
                        if (gain.compareTo(BigDecimal.ZERO) >= 0) totalStcgGains = totalStcgGains.add(gain);
                        else totalStcgLosses = totalStcgLosses.add(gain.abs());
                    } else {
                        if (gain.compareTo(BigDecimal.ZERO) >= 0) totalLtcgGains = totalLtcgGains.add(gain);
                        else totalLtcgLosses = totalLtcgLosses.add(gain.abs());
                    }

                    remainingQty = remainingQty.subtract(matchQty);
                    if (matchQty.compareTo(buy.getQuantity()) >= 0) {
                        buyQueue.poll(); // fully consumed
                    } else {
                        // Partially consumed — create a reduced buy entry
                        buyQueue.poll();
                        Transaction reduced = Transaction.builder()
                                .id(buy.getId())
                                .user(buy.getUser())
                                .stockSymbol(buy.getStockSymbol())
                                .stockName(buy.getStockName())
                                .type(buy.getType())
                                .quantity(buy.getQuantity().subtract(matchQty))
                                .price(buy.getPrice())
                                .transactionDate(buy.getTransactionDate())
                                .brokerage(buy.getBrokerage())
                                .build();
                        buyQueue.addFirst(reduced);
                    }
                }
            }
        }

        // Net STCG / LTCG
        BigDecimal netStcg = totalStcgGains.subtract(totalStcgLosses);
        BigDecimal netLtcg = totalLtcgGains.subtract(totalLtcgLosses);

        // STCG tax
        BigDecimal stcgTaxLiability = netStcg.compareTo(BigDecimal.ZERO) > 0
                ? FinancialUtils.calculateTaxLiability(netStcg, TaxType.STCG)
                : BigDecimal.ZERO;

        // LTCG tax (apply ₹1.25L exemption)
        BigDecimal taxableLtcg = netLtcg.subtract(FinancialUtils.LTCG_EXEMPTION)
                .max(BigDecimal.ZERO);
        BigDecimal ltcgTaxLiability = FinancialUtils.calculateTaxLiability(taxableLtcg, TaxType.LTCG);

        BigDecimal totalTax = stcgTaxLiability.add(ltcgTaxLiability);

        return TaxSummaryDTO.builder()
                .financialYear(fy)
                .totalStcgGains(totalStcgGains.setScale(2, RoundingMode.HALF_UP))
                .totalStcgLosses(totalStcgLosses.setScale(2, RoundingMode.HALF_UP))
                .netStcg(netStcg.setScale(2, RoundingMode.HALF_UP))
                .stcgTaxRate(FinancialUtils.STCG_RATE)
                .stcgTaxLiability(stcgTaxLiability.setScale(2, RoundingMode.HALF_UP))
                .totalLtcgGains(totalLtcgGains.setScale(2, RoundingMode.HALF_UP))
                .totalLtcgLosses(totalLtcgLosses.setScale(2, RoundingMode.HALF_UP))
                .netLtcg(netLtcg.setScale(2, RoundingMode.HALF_UP))
                .ltcgExemption(FinancialUtils.LTCG_EXEMPTION)
                .taxableLtcg(taxableLtcg.setScale(2, RoundingMode.HALF_UP))
                .ltcgTaxRate(FinancialUtils.LTCG_RATE)
                .ltcgTaxLiability(ltcgTaxLiability.setScale(2, RoundingMode.HALF_UP))
                .totalTaxLiability(totalTax.setScale(2, RoundingMode.HALF_UP))
                .totalRealised(netStcg.add(netLtcg).setScale(2, RoundingMode.HALF_UP))
                .lineItems(lineItems)
                .build();
    }

    /**
     * Delegates to portfolio service for sector allocation.
     */
    @Transactional(readOnly = true)
    public List<PortfolioAllocationDTO> getSectorAllocation(Long userId) {
        return portfolioService.getPortfolioAllocation(userId);
    }
}
