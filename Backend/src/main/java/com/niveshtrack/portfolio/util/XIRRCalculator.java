package com.niveshtrack.portfolio.util;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Calculates XIRR (Extended Internal Rate of Return) using the Newton-Raphson method.
 *
 * <p>XIRR gives an annualised rate of return that accounts for the timing of
 * cash flows, making it far more accurate than simple CAGR for SIP-style investing.
 *
 * <p>Convention:
 * <ul>
 *   <li>Outflows (purchases) are <b>negative</b></li>
 *   <li>Inflows (proceeds or current value) are <b>positive</b></li>
 * </ul>
 */
@Slf4j
public class XIRRCalculator {

    private static final int MAX_ITERATIONS = 1000;
    private static final double TOLERANCE = 1e-7;
    private static final double INITIAL_GUESS = 0.1;   // 10% initial guess

    private XIRRCalculator() {
        // Utility class – prevent instantiation
    }

    /**
     * Immutable representation of a single cash flow.
     *
     * @param date   date of the cash flow
     * @param amount negative for outflow (BUY), positive for inflow (SELL / current value)
     */
    public record CashFlow(LocalDate date, double amount) {}

    /**
     * Calculates XIRR for a list of cash flows.
     *
     * @param cashFlows list of CashFlow objects
     * @return annualised rate of return as a percentage (e.g., 15.43 means 15.43%), or null on failure
     */
    public static BigDecimal calculate(List<CashFlow> cashFlows) {
        if (cashFlows == null || cashFlows.size() < 2) {
            log.warn("XIRR requires at least 2 cash flows");
            return null;
        }

        // Validate: must have at least one negative and one positive flow
        boolean hasNegative = cashFlows.stream().anyMatch(cf -> cf.amount() < 0);
        boolean hasPositive = cashFlows.stream().anyMatch(cf -> cf.amount() > 0);
        if (!hasNegative || !hasPositive) {
            log.warn("XIRR requires at least one negative and one positive cash flow");
            return null;
        }

        LocalDate startDate = cashFlows.get(0).date();
        double rate = INITIAL_GUESS;

        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double npv = calculateNPV(cashFlows, startDate, rate);
            double dnpv = calculateDerivativeNPV(cashFlows, startDate, rate);

            if (Math.abs(dnpv) < 1e-15) {
                log.warn("XIRR: derivative too small, not converging");
                return null;
            }

            double newRate = rate - npv / dnpv;

            if (Math.abs(newRate - rate) < TOLERANCE) {
                // Converged
                double xirrPercent = newRate * 100;
                return BigDecimal.valueOf(xirrPercent).setScale(2, RoundingMode.HALF_UP);
            }

            rate = newRate;
        }

        log.warn("XIRR did not converge after {} iterations", MAX_ITERATIONS);
        return null;
    }

    /**
     * Net Present Value: sum of each flow discounted by (1+rate)^(days/365)
     */
    private static double calculateNPV(List<CashFlow> cashFlows, LocalDate startDate, double rate) {
        double npv = 0.0;
        for (CashFlow cf : cashFlows) {
            long days = ChronoUnit.DAYS.between(startDate, cf.date());
            double t = days / 365.0;
            npv += cf.amount() / Math.pow(1.0 + rate, t);
        }
        return npv;
    }

    /**
     * Derivative of NPV with respect to rate (used in Newton-Raphson step).
     */
    private static double calculateDerivativeNPV(List<CashFlow> cashFlows, LocalDate startDate, double rate) {
        double dNpv = 0.0;
        for (CashFlow cf : cashFlows) {
            long days = ChronoUnit.DAYS.between(startDate, cf.date());
            double t = days / 365.0;
            dNpv -= t * cf.amount() / Math.pow(1.0 + rate, t + 1.0);
        }
        return dNpv;
    }
}
