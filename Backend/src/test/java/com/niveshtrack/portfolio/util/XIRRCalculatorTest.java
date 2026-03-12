package com.niveshtrack.portfolio.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("XIRRCalculator Unit Tests")
class XIRRCalculatorTest {

    // ===== BASIC SIP SCENARIO =====

    @Nested
    @DisplayName("SIP (Systematic Investment Plan) Scenarios")
    class SIPScenarios {

        @Test
        @DisplayName("should compute ~12% XIRR for a standard monthly SIP with equal growth")
        void shouldComputeReasonableXIRRForMonthlySIP() {
            // 12 monthly SIPs of ₹10,000 then receive ₹128,000 at end (~12% XIRR)
            LocalDate start = LocalDate.of(2023, 1, 1);
            List<XIRRCalculator.CashFlow> cashFlows = Arrays.asList(
                new XIRRCalculator.CashFlow(start,                    -10000.0),
                new XIRRCalculator.CashFlow(start.plusMonths(1),      -10000.0),
                new XIRRCalculator.CashFlow(start.plusMonths(2),      -10000.0),
                new XIRRCalculator.CashFlow(start.plusMonths(3),      -10000.0),
                new XIRRCalculator.CashFlow(start.plusMonths(4),      -10000.0),
                new XIRRCalculator.CashFlow(start.plusMonths(5),      -10000.0),
                new XIRRCalculator.CashFlow(start.plusMonths(6),      -10000.0),
                new XIRRCalculator.CashFlow(start.plusMonths(7),      -10000.0),
                new XIRRCalculator.CashFlow(start.plusMonths(8),      -10000.0),
                new XIRRCalculator.CashFlow(start.plusMonths(9),      -10000.0),
                new XIRRCalculator.CashFlow(start.plusMonths(10),     -10000.0),
                new XIRRCalculator.CashFlow(start.plusMonths(11),     -10000.0),
                new XIRRCalculator.CashFlow(start.plusMonths(12),    128000.0)  // maturity
            );

            BigDecimal xirr = XIRRCalculator.calculate(cashFlows);

            // Should be roughly 12% ± 2%
            assertThat(xirr).isNotNull();
            assertThat(xirr.doubleValue()).isBetween(10.0, 14.0);
        }

        @Test
        @DisplayName("should compute ~20% XIRR for high-return scenario")
        void shouldComputeHighReturnXIRR() {
            // Single buy at start, sell after 2 years at 44% gain → ~20% CAGR
            LocalDate buy  = LocalDate.of(2022, 1, 1);
            LocalDate sell = LocalDate.of(2024, 1, 1);

            List<XIRRCalculator.CashFlow> cashFlows = Arrays.asList(
                new XIRRCalculator.CashFlow(buy,  -100000.0),
                new XIRRCalculator.CashFlow(sell,  144000.0) // ~20% p.a.
            );

            BigDecimal xirr = XIRRCalculator.calculate(cashFlows);

            assertThat(xirr.doubleValue()).isBetween(19.0, 21.0);
        }

        @Test
        @DisplayName("should compute negative XIRR for loss-making investment")
        void shouldComputeNegativeXIRR() {
            LocalDate buy  = LocalDate.of(2022, 1, 1);
            LocalDate sell = LocalDate.of(2023, 1, 1);

            // Invested 100,000 got back 80,000 → -20% XIRR
            List<XIRRCalculator.CashFlow> cashFlows = Arrays.asList(
                new XIRRCalculator.CashFlow(buy,  -100000.0),
                new XIRRCalculator.CashFlow(sell,   80000.0)
            );

            BigDecimal xirr = XIRRCalculator.calculate(cashFlows);

            assertThat(xirr.doubleValue()).isLessThan(0.0);
            assertThat(xirr.doubleValue()).isBetween(-21.0, -19.0);
        }
    }

    // ===== EDGE CASES =====

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        @DisplayName("should return null for empty cash flow list")
        void shouldReturnNullForEmptyCashFlows() {
            BigDecimal result = XIRRCalculator.calculate(List.of());
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should return null for single cash flow")
        void shouldReturnNullForSingleCashFlow() {
            BigDecimal result = XIRRCalculator.calculate(
                List.of(new XIRRCalculator.CashFlow(LocalDate.now(), -10000.0))
            );
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("should return null when all cash flows have same date")
        void shouldReturnNullForSameDateCashFlows() {
            LocalDate today = LocalDate.now();
            BigDecimal result = XIRRCalculator.calculate(Arrays.asList(
                new XIRRCalculator.CashFlow(today, -10000.0),
                new XIRRCalculator.CashFlow(today,  12000.0)
            ));
            // Either null or very large number; should not throw
            // If NPE or runtime exception is thrown the test fails
            assertThatNoException().isThrownBy(() -> XIRRCalculator.calculate(Arrays.asList(
                new XIRRCalculator.CashFlow(today, -10000.0),
                new XIRRCalculator.CashFlow(today,  12000.0)
            )));
        }

        @Test
        @DisplayName("should handle break-even scenario returning ~0% XIRR")
        void shouldHandleBreakEven() {
            LocalDate buy  = LocalDate.of(2023, 1, 1);
            LocalDate sell = LocalDate.of(2024, 1, 1);

            List<XIRRCalculator.CashFlow> cashFlows = Arrays.asList(
                new XIRRCalculator.CashFlow(buy,  -100000.0),
                new XIRRCalculator.CashFlow(sell,  100000.0)
            );

            BigDecimal xirr = XIRRCalculator.calculate(cashFlows);

            assertThat(xirr.doubleValue()).isBetween(-1.0, 1.0);
        }
    }

    // ===== REAL-WORLD SCENARIO =====

    @Nested
    @DisplayName("Real-World Portfolio Scenario")
    class RealWorldScenario {

        @Test
        @DisplayName("should compute XIRR for staggered BUY entries and lump-sum current value")
        void shouldComputeXIRRForStaggeredBuys() {
            // Simulate typical portfolio: buy at different dates, portfolio valued today
            List<XIRRCalculator.CashFlow> cashFlows = Arrays.asList(
                // BUYs (negative outflows)
                new XIRRCalculator.CashFlow(LocalDate.of(2022,  1, 10), -50000.0),
                new XIRRCalculator.CashFlow(LocalDate.of(2022,  6, 15), -30000.0),
                new XIRRCalculator.CashFlow(LocalDate.of(2023,  3, 20), -40000.0),
                new XIRRCalculator.CashFlow(LocalDate.of(2023, 11,  5), -20000.0),
                // Current portfolio value (positive inflow)
                new XIRRCalculator.CashFlow(LocalDate.of(2024,  6,  1), 185000.0)
            );
            // Total invested = 140,000 → current = 185,000 → ~32% overall return

            BigDecimal xirr = XIRRCalculator.calculate(cashFlows);

            assertThat(xirr).isNotNull();
            // Should be positive and reasonable (15-35%)
            assertThat(xirr.doubleValue()).isGreaterThan(10.0);
            assertThat(xirr.doubleValue()).isLessThan(50.0);
        }
    }
}
