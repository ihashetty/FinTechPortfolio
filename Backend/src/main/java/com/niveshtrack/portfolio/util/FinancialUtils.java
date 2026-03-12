package com.niveshtrack.portfolio.util;

import com.niveshtrack.portfolio.entity.TaxType;
import com.niveshtrack.portfolio.entity.Transaction;
import com.niveshtrack.portfolio.entity.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Financial calculation utilities for portfolio metrics.
 *
 * <p>Indian Tax Rules (equity, post-July 23, 2024 Budget):
 * <ul>
 *   <li>STCG (held &lt; 365 days): 20% flat</li>
 *   <li>LTCG (held ≥ 365 days): 12.5% on gains exceeding ₹1,25,000 exemption</li>
 * </ul>
 */
public class FinancialUtils {

    // Indian tax rates (post Budget 2024)
    public static final BigDecimal STCG_RATE = new BigDecimal("20.0");
    public static final BigDecimal LTCG_RATE = new BigDecimal("12.5");
    public static final BigDecimal LTCG_EXEMPTION = new BigDecimal("125000"); // ₹1.25 lakh

    private FinancialUtils() {
        // Utility class
    }

    /**
     * Calculates the weighted average buy price for a list of transactions.
     *
     * <p>Formula: SUM(price × quantity) / SUM(quantity) considering only BUY transactions.
     *
     * @param transactions all transactions for a single stock
     * @return weighted average buy price, or ZERO if no BUY transactions exist
     */
    public static BigDecimal calculateWeightedAverage(List<Transaction> transactions) {
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalQty = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.BUY) {
                totalCost = totalCost.add(t.getPrice().multiply(t.getQuantity()));
                totalQty = totalQty.add(t.getQuantity());
            }
        }

        if (totalQty.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return totalCost.divide(totalQty, 4, RoundingMode.HALF_UP);
    }

    /**
     * Returns the holding period in calendar days.
     */
    public static long calculateHoldingPeriodDays(java.time.LocalDate buyDate,
                                                   java.time.LocalDate sellDate) {
        return DateUtils.daysBetween(buyDate, sellDate);
    }

    /**
     * Classifies the gain as STCG or LTCG based on holding period.
     *
     * @param holdingPeriodDays number of days held
     * @return {@link TaxType#STCG} if held &lt; 365 days, else {@link TaxType#LTCG}
     */
    public static TaxType classifyTaxType(long holdingPeriodDays) {
        return holdingPeriodDays < 365 ? TaxType.STCG : TaxType.LTCG;
    }

    /**
     * Calculates the tax liability on a given gain.
     *
     * <p>For LTCG, the ₹1.25 lakh exemption must be applied externally before calling this
     * method (i.e., pass the taxable LTCG amount after deducting the exemption).
     *
     * @param gain     the net gain amount (must be positive)
     * @param taxType  STCG or LTCG
     * @return tax amount rounded to 2 decimal places
     */
    public static BigDecimal calculateTaxLiability(BigDecimal gain, TaxType taxType) {
        if (gain.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal rate = taxType == TaxType.STCG ? STCG_RATE : LTCG_RATE;
        return gain.multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates return percentage: (gainLoss / investedAmount) × 100.
     *
     * @return return percentage with 2 decimal places, or ZERO if investedAmount is 0
     */
    public static BigDecimal calculateReturnPercent(BigDecimal gainLoss, BigDecimal investedAmount) {
        if (investedAmount == null || investedAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return gainLoss.divide(investedAmount, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Safe division that returns ZERO on divide-by-zero.
     */
    public static BigDecimal safeDivide(BigDecimal numerator, BigDecimal denominator, int scale) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, scale, RoundingMode.HALF_UP);
    }
}
