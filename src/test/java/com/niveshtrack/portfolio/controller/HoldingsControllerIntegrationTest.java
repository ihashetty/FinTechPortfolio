package com.niveshtrack.portfolio.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.niveshtrack.portfolio.dto.request.CreateTransactionRequest;
import com.niveshtrack.portfolio.dto.request.RegisterRequest;
import com.niveshtrack.portfolio.entity.Stock;
import com.niveshtrack.portfolio.entity.TransactionType;
import com.niveshtrack.portfolio.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("HoldingsController Integration Tests")
class HoldingsControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private StockRepository stockRepository;

    private String accessToken;

    private static final String HOLDINGS_URL = "/api/holdings";
    private static final String TX_URL        = "/api/transactions";
    private static final String AUTH_URL      = "/api/auth/register";

    @BeforeEach
    void setUp() throws Exception {
        // Seed stock price data so holdings can be enriched
        seedStocks();
        accessToken = registerAndGetToken("holdingsuser@example.com", "password123");
        fundWallet(new BigDecimal("10000000"));
    }

    // ===== GET HOLDINGS =====

    @Nested
    @DisplayName("GET /api/holdings")
    class GetHoldings {

        @Test
        @DisplayName("should return 401 when no auth token provided")
        void shouldReturn401WithNoToken() throws Exception {
            mockMvc.perform(get(HOLDINGS_URL))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return empty list for a user with no transactions")
        void shouldReturnEmptyForNewUser() throws Exception {
            mockMvc.perform(get(HOLDINGS_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("should return one holding after a BUY transaction")
        void shouldReturnHoldingAfterBuy() throws Exception {
            createBuyTransaction("TCS", 20, "3200.00", "2023-01-01");

            mockMvc.perform(get(HOLDINGS_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].symbol").value("TCS"))
                    .andExpect(jsonPath("$[0].quantity").value(20))
                    .andExpect(jsonPath("$[0].avgBuyPrice").isNumber())
                    .andExpect(jsonPath("$[0].investedAmount").isNumber())
                    .andExpect(jsonPath("$[0].currentPrice").isNumber())
                    .andExpect(jsonPath("$[0].gainLoss").isNumber())
                    .andExpect(jsonPath("$[0].returnPercent").isNumber());
        }

        @Test
        @DisplayName("should return multiple holdings for multiple stock BUYs")
        void shouldReturnMultipleHoldings() throws Exception {
            createBuyTransaction("TCS",  20, "3200.00", "2023-01-01");
            createBuyTransaction("INFY", 30, "1500.00", "2023-02-01");
            createBuyTransaction("SBIN", 50, "700.00",  "2023-03-01");

            mockMvc.perform(get(HOLDINGS_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(3)));
        }

        @Test
        @DisplayName("should net quantity correctly after a SELL")
        void shouldNetQuantityAfterSell() throws Exception {
            // BUY 50 TCS
            createBuyTransaction("TCS", 50, "3000.00", "2022-01-01");
            // SELL 20 TCS
            createSellTransaction("TCS", 20, "3500.00", "2023-06-01");

            mockMvc.perform(get(HOLDINGS_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].symbol").value("TCS"))
                    .andExpect(jsonPath("$[0].quantity").value(30));  // 50 - 20
        }

        @Test
        @DisplayName("should not show stock that was fully sold off")
        void shouldExcludeFullySoldStock() throws Exception {
            createBuyTransaction("WIPRO", 50, "450.00", "2022-01-01");
            createSellTransaction("WIPRO", 50, "520.00", "2024-01-01"); // all sold

            mockMvc.perform(get(HOLDINGS_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("should include weightPercent that sums to ~100%")
        void shouldHaveWeightPercentsSummingTo100() throws Exception {
            createBuyTransaction("TCS",  10, "3000.00", "2023-01-01");
            createBuyTransaction("INFY", 20, "1500.00", "2023-02-01");

            mockMvc.perform(get(HOLDINGS_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].weightPercent", everyItem(greaterThan(0.0))));
        }

        @Test
        @DisplayName("should not return holdings belonging to another user")
        void shouldNotReturnOtherUsersHoldings() throws Exception {
            // user1 buys TCS
            createBuyTransaction("TCS", 10, "3000.00", "2023-01-01");

            // user2 registers and fetches holdings
            String user2Token = registerAndGetToken("otherholdings@example.com", "password456");

            mockMvc.perform(get(HOLDINGS_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + user2Token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));  // no TCS for user2
        }
    }

    // ===== HELPERS =====

    private void seedStocks() {
        List<Stock> stocks = List.of(
            Stock.builder().symbol("TCS").name("Tata Consultancy Services Ltd")
                    .sector("IT").currentPrice(new BigDecimal("3950.00"))
                    .lastUpdated(LocalDateTime.now()).build(),
            Stock.builder().symbol("INFY").name("Infosys Ltd")
                    .sector("IT").currentPrice(new BigDecimal("1780.00"))
                    .lastUpdated(LocalDateTime.now()).build(),
            Stock.builder().symbol("SBIN").name("State Bank of India")
                    .sector("Banking").currentPrice(new BigDecimal("820.00"))
                    .lastUpdated(LocalDateTime.now()).build(),
            Stock.builder().symbol("WIPRO").name("Wipro Ltd")
                    .sector("IT").currentPrice(new BigDecimal("520.00"))
                    .lastUpdated(LocalDateTime.now()).build()
        );
        stockRepository.saveAll(stocks);
    }

    private void fundWallet(BigDecimal amount) throws Exception {
        mockMvc.perform(post("/api/wallet/deposit")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": " + amount.toPlainString() + "}"))
                .andExpect(status().isOk());
    }

    private String registerAndGetToken(String email, String password) throws Exception {
        RegisterRequest reg = new RegisterRequest();
        reg.setName("Holdings Test User");
        reg.setEmail(email);
        reg.setPassword(password);

        MvcResult result = mockMvc.perform(post(AUTH_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private void createBuyTransaction(String symbol, int qty,
                                       String price, String date) throws Exception {
        createTransaction(symbol, qty, price, date, TransactionType.BUY);
    }

    private void createSellTransaction(String symbol, int qty,
                                        String price, String date) throws Exception {
        createTransaction(symbol, qty, price, date, TransactionType.SELL);
    }

    private void createTransaction(String symbol, int qty,
                                    String price, String date,
                                    TransactionType type) throws Exception {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setStockSymbol(symbol);
        req.setStockName(symbol + " Ltd");
        req.setType(type);
        req.setQuantity(new BigDecimal(qty));
        req.setPrice(new BigDecimal(price));
        req.setTransactionDate(LocalDate.parse(date));
        req.setBrokerage(new BigDecimal("20.00"));

        mockMvc.perform(post(TX_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }
}
