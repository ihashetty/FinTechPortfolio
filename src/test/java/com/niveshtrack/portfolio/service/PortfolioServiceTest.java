package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.response.DashboardSummaryDTO;
import com.niveshtrack.portfolio.dto.response.HoldingDTO;
import com.niveshtrack.portfolio.dto.response.PortfolioAllocationDTO;
import com.niveshtrack.portfolio.entity.PortfolioSnapshot;
import com.niveshtrack.portfolio.entity.User;
import com.niveshtrack.portfolio.repository.SipInstructionRepository;
import com.niveshtrack.portfolio.repository.SnapshotRepository;
import com.niveshtrack.portfolio.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioService Unit Tests")
class PortfolioServiceTest {

    @Mock
    private HoldingsService holdingsService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private SipInstructionRepository sipInstructionRepository;

    @Mock
    private SnapshotRepository snapshotRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    private User demoUser;

    @BeforeEach
    void setUp() {
        demoUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .currency("INR")
                .build();
    }

    // ===== DASHBOARD SUMMARY =====

    @Nested
    @DisplayName("Dashboard Summary")
    class Dashboard {

        @Test
        @DisplayName("should return zeroed summary when user has no holdings")
        void shouldReturnZeroSummaryForEmptyPortfolio() {
            given(holdingsService.calculateHoldings(1L)).willReturn(List.of());

            DashboardSummaryDTO summary = portfolioService.getDashboardSummary(1L);

            assertThat(summary.getCurrentValue()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.getTotalInvested()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.getTotalPL()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.getTotalHoldings()).isZero();
            assertThat(summary.getTopGainer()).isNull();
            assertThat(summary.getTopLoser()).isNull();
        }

        @Test
        @DisplayName("should aggregate total value and invested correctly")
        void shouldAggregateTotals() {
            HoldingDTO h1 = holding("TCS",     "IT",      "79000.00", "64000.00",  "15000.00");
            HoldingDTO h2 = holding("HDFCBANK", "Banking", "82500.00", "77500.00",  "5000.00");
            HoldingDTO h3 = holding("INFY",     "IT",      "71200.00", "87000.00", "-15800.00");

            given(holdingsService.calculateHoldings(1L)).willReturn(Arrays.asList(h1, h2, h3));

            DashboardSummaryDTO summary = portfolioService.getDashboardSummary(1L);

            // 79000 + 82500 + 71200 = 232700
            assertThat(summary.getCurrentValue()).isEqualByComparingTo("232700.00");
            // 64000 + 77500 + 87000 = 228500
            assertThat(summary.getTotalInvested()).isEqualByComparingTo("228500.00");
            // 15000 + 5000 - 15800 = 4200
            assertThat(summary.getTotalPL()).isEqualByComparingTo("4200.00");
            assertThat(summary.getTotalHoldings()).isEqualTo(3);
        }

        @Test
        @DisplayName("should identify top gainer correctly")
        void shouldIdentifyTopGainer() {
            HoldingDTO h1 = holding("TCS",  "IT",  "79000.00", "64000.00", "15000.00");  // +23.4%
            HoldingDTO h2 = holding("INFY", "IT",  "60000.00", "45000.00", "15000.00");  // +33.3%
            HoldingDTO h3 = holding("SBIN", "Banking", "50000.00", "70000.00", "-20000.00");

            // Set return percent manually
            h1.setReturnPercent(new BigDecimal("23.4"));
            h2.setReturnPercent(new BigDecimal("33.3"));
            h3.setReturnPercent(new BigDecimal("-28.6"));

            given(holdingsService.calculateHoldings(1L)).willReturn(Arrays.asList(h1, h2, h3));

            DashboardSummaryDTO summary = portfolioService.getDashboardSummary(1L);

            assertThat(summary.getTopGainer()).isNotNull();
            assertThat(summary.getTopGainer().getSymbol()).isEqualTo("INFY");
        }

        @Test
        @DisplayName("should identify top loser correctly")
        void shouldIdentifyTopLoser() {
            HoldingDTO h1 = holding("TCS",  "IT",  "79000.00", "64000.00",  "15000.00");
            HoldingDTO h2 = holding("WIPRO", "IT", "40000.00", "50000.00", "-10000.00");
            HoldingDTO h3 = holding("SBIN", "Banking", "30000.00", "55000.00", "-25000.00");

            h1.setReturnPercent(new BigDecimal("23.4"));
            h2.setReturnPercent(new BigDecimal("-20.0"));
            h3.setReturnPercent(new BigDecimal("-45.5"));

            given(holdingsService.calculateHoldings(1L)).willReturn(Arrays.asList(h1, h2, h3));

            DashboardSummaryDTO summary = portfolioService.getDashboardSummary(1L);

            assertThat(summary.getTopLoser()).isNotNull();
            assertThat(summary.getTopLoser().getSymbol()).isEqualTo("SBIN");
        }

        @Test
        @DisplayName("should identify largest holding by total value")
        void shouldIdentifyLargestHolding() {
            HoldingDTO h1 = holding("TCS",      "IT",      "200000.00", "160000.00", "40000.00");
            HoldingDTO h2 = holding("HDFCBANK",  "Banking", "150000.00", "140000.00", "10000.00");
            HoldingDTO h3 = holding("RELIANCE",  "Energy",  "120000.00", "100000.00", "20000.00");

            given(holdingsService.calculateHoldings(1L)).willReturn(Arrays.asList(h1, h2, h3));

            DashboardSummaryDTO summary = portfolioService.getDashboardSummary(1L);

            assertThat(summary.getLargestHolding()).isNotNull();
            assertThat(summary.getLargestHolding().getSymbol()).isEqualTo("TCS");
        }
    }

    // ===== SECTOR ALLOCATION =====

    @Nested
    @DisplayName("Portfolio Allocation")
    class Allocation {

        @Test
        @DisplayName("should group holdings by sector and compute percentages")
        void shouldGroupBySector() {
            HoldingDTO tcs      = holding("TCS",      "IT",      "80000.00",  "64000.00",  "16000.00");
            HoldingDTO infy     = holding("INFY",     "IT",      "60000.00",  "54000.00",  "6000.00");
            HoldingDTO hdfcbank = holding("HDFCBANK", "Banking", "100000.00", "90000.00",  "10000.00");

            given(holdingsService.calculateHoldings(1L)).willReturn(Arrays.asList(tcs, infy, hdfcbank));

            List<PortfolioAllocationDTO> allocation = portfolioService.getPortfolioAllocation(1L);

            assertThat(allocation).hasSize(2); // IT and Banking

            PortfolioAllocationDTO itAlloc = allocation.stream()
                    .filter(a -> "IT".equals(a.getSector())).findFirst().orElseThrow();
            PortfolioAllocationDTO bankAlloc = allocation.stream()
                    .filter(a -> "Banking".equals(a.getSector())).findFirst().orElseThrow();

            // IT = 80000+60000 = 140000, Banking = 100000, total = 240000
            // IT% = 58.33, Banking% = 41.67
            assertThat(itAlloc.getTotalValue()).isEqualByComparingTo("140000.00");
            assertThat(bankAlloc.getTotalValue()).isEqualByComparingTo("100000.00");

            double totalPercent = allocation.stream()
                    .mapToDouble(a -> a.getPercentage().doubleValue())
                    .sum();
            assertThat(totalPercent).isCloseTo(100.0, org.assertj.core.data.Offset.offset(0.1));
        }

        @Test
        @DisplayName("should count stocks per sector correctly")
        void shouldCountStocksPerSector() {
            HoldingDTO tcs   = holding("TCS",   "IT",  "80000.00", "64000.00", "16000.00");
            HoldingDTO infy  = holding("INFY",  "IT",  "60000.00", "54000.00",  "6000.00");
            HoldingDTO wipro = holding("WIPRO", "IT",  "40000.00", "45000.00", "-5000.00");
            HoldingDTO hdfc  = holding("HDFCBANK", "Banking", "100000.00", "90000.00", "10000.00");

            given(holdingsService.calculateHoldings(1L)).willReturn(Arrays.asList(tcs, infy, wipro, hdfc));

            List<PortfolioAllocationDTO> allocation = portfolioService.getPortfolioAllocation(1L);

            PortfolioAllocationDTO itAlloc = allocation.stream()
                    .filter(a -> "IT".equals(a.getSector())).findFirst().orElseThrow();

            assertThat(itAlloc.getStockCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("should return empty allocation for empty portfolio")
        void shouldReturnEmptyForNoHoldings() {
            given(holdingsService.calculateHoldings(1L)).willReturn(List.of());

            List<PortfolioAllocationDTO> allocation = portfolioService.getPortfolioAllocation(1L);

            assertThat(allocation).isEmpty();
        }
    }

    // ===== HELPERS =====

    private HoldingDTO holding(String symbol, String sector,
                                String totalValue, String invested, String gainLoss) {
        HoldingDTO h = new HoldingDTO();
        h.setSymbol(symbol);
        h.setName(symbol + " Ltd");
        h.setSector(sector);
        h.setTotalValue(new BigDecimal(totalValue));
        h.setInvestedAmount(new BigDecimal(invested));
        h.setGainLoss(new BigDecimal(gainLoss));

        BigDecimal investedBD = new BigDecimal(invested);
        if (investedBD.compareTo(BigDecimal.ZERO) != 0) {
            h.setReturnPercent(new BigDecimal(gainLoss)
                    .divide(investedBD, 4, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)));
        } else {
            h.setReturnPercent(BigDecimal.ZERO);
        }
        return h;
    }
}
