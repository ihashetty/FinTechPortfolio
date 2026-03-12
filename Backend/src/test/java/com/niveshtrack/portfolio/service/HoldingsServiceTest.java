package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.response.HoldingDTO;
import com.niveshtrack.portfolio.entity.Stock;
import com.niveshtrack.portfolio.entity.Transaction;
import com.niveshtrack.portfolio.entity.TransactionType;
import com.niveshtrack.portfolio.entity.User;
import com.niveshtrack.portfolio.repository.HoldingRepository;
import com.niveshtrack.portfolio.repository.StockRepository;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("HoldingsService Unit Tests")
class HoldingsServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private HoldingRepository holdingRepository;

    @Mock
    private HoldingService holdingService;

    @InjectMocks
    private HoldingsService holdingsService;

    private User demoUser;

    @BeforeEach
    void setUp() {
        demoUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hashed")
                .currency("INR")
                .build();
    }

    // ===== HOLDINGS CALCULATION =====

    @Nested
    @DisplayName("Holdings Calculation (legacy fallback)")
    class HoldingsCalculation {

        @Test
        @DisplayName("should return empty list when user has no transactions")
        void shouldReturnEmptyWhenNoTransactions() {
            // No persisted holdings
            given(holdingRepository.findByUserId(1L)).willReturn(List.of());
            given(transactionRepository.findByUserIdOrderByTransactionDateDesc(1L))
                    .willReturn(List.of());

            List<HoldingDTO> result = holdingsService.calculateHoldings(1L);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should calculate holding for a pure BUY position")
        void shouldCalculatePureBuyPosition() {
            given(holdingRepository.findByUserId(1L)).willReturn(List.of());

            Transaction buy = buy("TCS", 20, "3200.00", "2023-06-01");

            given(transactionRepository.findByUserIdOrderByTransactionDateDesc(1L))
                    .willReturn(List.of(buy));

            Stock tcs = stock("TCS", "Tata Consultancy Services Ltd", "IT", "3950.00");
            given(stockRepository.findById("TCS")).willReturn(Optional.of(tcs));

            List<HoldingDTO> holdings = holdingsService.calculateHoldings(1L);

            assertThat(holdings).hasSize(1);
            HoldingDTO h = holdings.get(0);
            assertThat(h.getSymbol()).isEqualTo("TCS");
            assertThat(h.getQuantity()).isEqualByComparingTo(new BigDecimal("20"));
            assertThat(h.getAvgBuyPrice()).isEqualByComparingTo("3200.00");
            assertThat(h.getInvestedAmount()).isEqualByComparingTo("64000.00");
            assertThat(h.getCurrentPrice()).isEqualByComparingTo("3950.00");
            assertThat(h.getTotalValue()).isEqualByComparingTo("79000.00");
            assertThat(h.getGainLoss()).isEqualByComparingTo("15000.00");
        }

        @Test
        @DisplayName("should net BUY and SELL to derive correct quantity")
        void shouldNetBuySell() {
            given(holdingRepository.findByUserId(1L)).willReturn(List.of());

            Transaction buy = buy("RELIANCE", 50, "2400.00", "2023-01-10");
            Transaction sell = sell("RELIANCE", 10, "2700.00", "2023-09-14");

            given(transactionRepository.findByUserIdOrderByTransactionDateDesc(1L))
                    .willReturn(Arrays.asList(buy, sell));

            Stock reliance = stock("RELIANCE", "Reliance Industries Ltd", "Energy", "2850.00");
            given(stockRepository.findById("RELIANCE")).willReturn(Optional.of(reliance));

            List<HoldingDTO> holdings = holdingsService.calculateHoldings(1L);

            assertThat(holdings).hasSize(1);
            assertThat(holdings.get(0).getQuantity()).isEqualByComparingTo(new BigDecimal("40"));
        }

        @Test
        @DisplayName("should exclude symbols fully sold out")
        void shouldExcludeFullySoldPositions() {
            given(holdingRepository.findByUserId(1L)).willReturn(List.of());

            Transaction buy = buy("WIPRO", 50, "450.00", "2023-06-01");
            Transaction sell = sell("WIPRO", 50, "520.00", "2024-05-15");

            given(transactionRepository.findByUserIdOrderByTransactionDateDesc(1L))
                    .willReturn(Arrays.asList(buy, sell));

            List<HoldingDTO> holdings = holdingsService.calculateHoldings(1L);

            assertThat(holdings).isEmpty();
        }

        @Test
        @DisplayName("should calculate weighted average buy price across multiple buys")
        void shouldCalculateWeightedAverageBuyPrice() {
            given(holdingRepository.findByUserId(1L)).willReturn(List.of());

            Transaction buy1 = buy("TCS", 20, "3200.00", "2023-06-01");
            Transaction buy2 = buy("TCS", 10, "3400.00", "2023-07-18");

            given(transactionRepository.findByUserIdOrderByTransactionDateDesc(1L))
                    .willReturn(Arrays.asList(buy1, buy2));

            Stock tcs = stock("TCS", "Tata Consultancy Services Ltd", "IT", "3950.00");
            given(stockRepository.findById("TCS")).willReturn(Optional.of(tcs));

            List<HoldingDTO> holdings = holdingsService.calculateHoldings(1L);

            assertThat(holdings).hasSize(1);
            assertThat(holdings.get(0).getAvgBuyPrice())
                    .isEqualByComparingTo(new BigDecimal("3266.67"));
            assertThat(holdings.get(0).getQuantity()).isEqualByComparingTo(new BigDecimal("30"));
        }

        @Test
        @DisplayName("should handle multiple stocks and compute weight percentages correctly")
        void shouldComputeWeightPercents() {
            given(holdingRepository.findByUserId(1L)).willReturn(List.of());

            Transaction tcsBuy = buy("TCS", 10, "3000.00", "2023-01-01");
            Transaction infyBuy = buy("INFY", 20, "1500.00", "2023-02-01");

            given(transactionRepository.findByUserIdOrderByTransactionDateDesc(1L))
                    .willReturn(Arrays.asList(tcsBuy, infyBuy));

            given(stockRepository.findById("TCS"))
                    .willReturn(Optional.of(stock("TCS", "TCS", "IT", "3000.00")));
            given(stockRepository.findById("INFY"))
                    .willReturn(Optional.of(stock("INFY", "Infosys Ltd", "IT", "1500.00")));

            List<HoldingDTO> holdings = holdingsService.calculateHoldings(1L);

            assertThat(holdings).hasSize(2);
            double totalWeight = holdings.stream()
                    .mapToDouble(h -> h.getWeightPercent().doubleValue())
                    .sum();
            assertThat(totalWeight).isCloseTo(100.0, offset(0.1));
        }

        @Test
        @DisplayName("should use stock symbol as name when stock not found in DB")
        void shouldFallbackWhenStockNotFound() {
            given(holdingRepository.findByUserId(1L)).willReturn(List.of());

            Transaction buy = buy("NEWSTOCK", 5, "100.00", "2024-01-01");

            given(transactionRepository.findByUserIdOrderByTransactionDateDesc(1L))
                    .willReturn(List.of(buy));
            given(stockRepository.findById("NEWSTOCK")).willReturn(Optional.empty());

            List<HoldingDTO> holdings = holdingsService.calculateHoldings(1L);

            assertThat(holdings).hasSize(1);
            assertThat(holdings.get(0).getSymbol()).isEqualTo("NEWSTOCK");
        }
    }

    // ===== RETURN CALCULATION =====

    @Nested
    @DisplayName("Return Calculation")
    class ReturnCalculation {

        @Test
        @DisplayName("should compute positive return percentage for gain")
        void shouldComputePositiveReturn() {
            given(holdingRepository.findByUserId(1L)).willReturn(List.of());

            Transaction buy = buy("SBIN", 100, "700.00", "2023-01-01");

            given(transactionRepository.findByUserIdOrderByTransactionDateDesc(1L))
                    .willReturn(List.of(buy));
            given(stockRepository.findById("SBIN"))
                    .willReturn(Optional.of(stock("SBIN", "State Bank of India", "Banking", "820.00")));

            List<HoldingDTO> holdings = holdingsService.calculateHoldings(1L);
            HoldingDTO h = holdings.get(0);

            assertThat(h.getReturnPercent()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should compute negative return percentage for loss")
        void shouldComputeNegativeReturn() {
            given(holdingRepository.findByUserId(1L)).willReturn(List.of());

            Transaction buy = buy("TECHM", 50, "1800.00", "2023-01-01");

            given(transactionRepository.findByUserIdOrderByTransactionDateDesc(1L))
                    .willReturn(List.of(buy));
            given(stockRepository.findById("TECHM"))
                    .willReturn(Optional.of(stock("TECHM", "Tech Mahindra Ltd", "IT", "1580.00")));

            List<HoldingDTO> holdings = holdingsService.calculateHoldings(1L);
            HoldingDTO h = holdings.get(0);

            assertThat(h.getReturnPercent()).isLessThan(BigDecimal.ZERO);
            assertThat(h.getGainLoss()).isLessThan(BigDecimal.ZERO);
        }
    }

    // ===== Helpers =====

    private Transaction buy(String symbol, int qty, String price, String date) {
        return Transaction.builder()
                .id((long) symbol.hashCode() + qty)
                .user(demoUser)
                .stockSymbol(symbol)
                .stockName(symbol + " Ltd")
                .type(TransactionType.BUY)
                .quantity(new BigDecimal(qty))
                .price(new BigDecimal(price))
                .transactionDate(LocalDate.parse(date))
                .brokerage(BigDecimal.ZERO)
                .build();
    }

    private Transaction sell(String symbol, int qty, String price, String date) {
        return Transaction.builder()
                .id((long) symbol.hashCode() + qty + 1000)
                .user(demoUser)
                .stockSymbol(symbol)
                .stockName(symbol + " Ltd")
                .type(TransactionType.SELL)
                .quantity(new BigDecimal(qty))
                .price(new BigDecimal(price))
                .transactionDate(LocalDate.parse(date))
                .brokerage(BigDecimal.ZERO)
                .build();
    }

    private Stock stock(String symbol, String name, String sector, String price) {
        return Stock.builder()
                .symbol(symbol)
                .name(name)
                .sector(sector)
                .currentPrice(new BigDecimal(price))
                .build();
    }

    private static org.assertj.core.data.Offset<Double> within(double delta) {
        return org.assertj.core.data.Offset.offset(delta);
    }
}
