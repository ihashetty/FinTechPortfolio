package com.niveshtrack.portfolio.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niveshtrack.portfolio.dto.request.CreateTransactionRequest;
import com.niveshtrack.portfolio.dto.request.LoginRequest;
import com.niveshtrack.portfolio.dto.request.RegisterRequest;
import com.niveshtrack.portfolio.dto.request.UpdateTransactionRequest;
import com.niveshtrack.portfolio.entity.Stock;
import com.niveshtrack.portfolio.entity.TransactionType;
import com.niveshtrack.portfolio.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
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
@DisplayName("TransactionController Integration Tests")
class TransactionControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private StockRepository stockRepository;

    private String accessToken;

    private static final String TX_URL   = "/api/transactions";
    private static final String AUTH_URL  = "/api/auth";

    @BeforeEach
    void setUp() throws Exception {
        seedStocks();
        accessToken = registerAndGetToken("txuser@example.com", "password123");
        fundWallet(new BigDecimal("10000000"));
    }

    // ===== CREATE =====

    @Nested
    @DisplayName("POST /api/transactions")
    class CreateTransaction {

        @Test
        @DisplayName("should create a BUY transaction and return 201")
        void shouldCreateBuyTransaction() throws Exception {
            CreateTransactionRequest req = buildBuyRequest("RELIANCE", 10,
                    new BigDecimal("2500.00"), LocalDate.of(2023, 1, 1));

            mockMvc.perform(post(TX_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").isNumber())
                    .andExpect(jsonPath("$.stockSymbol").value("RELIANCE"))
                    .andExpect(jsonPath("$.type").value("BUY"))
                    .andExpect(jsonPath("$.quantity").value(10))
                    .andExpect(jsonPath("$.totalAmount").isNumber());
        }

        @Test
        @DisplayName("should return 401 when no auth token provided")
        void shouldReturn401WhenNoAuth() throws Exception {
            CreateTransactionRequest req = buildBuyRequest("TCS", 5,
                    new BigDecimal("3000.00"), LocalDate.now());

            mockMvc.perform(post(TX_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 400 when quantity is zero or negative")
        void shouldReturn400WhenQuantityInvalid() throws Exception {
            CreateTransactionRequest req = new CreateTransactionRequest();
            req.setStockSymbol("TCS");
            req.setStockName("TCS Ltd");
            req.setType(TransactionType.BUY);
            req.setQuantity(BigDecimal.ZERO);  // invalid
            req.setPrice(new BigDecimal("3000.00"));
            req.setTransactionDate(LocalDate.now());

            mockMvc.perform(post(TX_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when price is negative")
        void shouldReturn400WhenPriceNegative() throws Exception {
            CreateTransactionRequest req = new CreateTransactionRequest();
            req.setStockSymbol("TCS");
            req.setStockName("TCS Ltd");
            req.setType(TransactionType.BUY);
            req.setQuantity(new BigDecimal("10"));
            req.setPrice(new BigDecimal("-100.00"));  // invalid
            req.setTransactionDate(LocalDate.now());

            mockMvc.perform(post(TX_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when selling more than available")
        void shouldReturn400WhenSellExceedsHoldings() throws Exception {
            // No prior BUY; attempting SELL should fail with validation error
            CreateTransactionRequest req = new CreateTransactionRequest();
            req.setStockSymbol("INFY");
            req.setStockName("Infosys Ltd");
            req.setType(TransactionType.SELL);
            req.setQuantity(new BigDecimal("10"));
            req.setPrice(new BigDecimal("1800.00"));
            req.setTransactionDate(LocalDate.now());
            req.setBrokerage(BigDecimal.ZERO);

            mockMvc.perform(post(TX_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest());
        }
    }

    // ===== GET ALL =====

    @Nested
    @DisplayName("GET /api/transactions")
    class GetTransactions {

        @Test
        @DisplayName("should return empty list for new user")
        void shouldReturnEmptyForNewUser() throws Exception {
            mockMvc.perform(get(TX_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("should return created transactions")
        void shouldReturnCreatedTransactions() throws Exception {
            // Create a transaction first
            createBuyTransaction("TCS", 10, "3000.00", "2023-01-01");

            mockMvc.perform(get(TX_URL)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("should filter transactions by symbol")
        void shouldFilterBySymbol() throws Exception {
            createBuyTransaction("TCS",  10, "3000.00", "2023-01-01");
            createBuyTransaction("INFY", 20, "1500.00", "2023-02-01");

            mockMvc.perform(get(TX_URL).param("symbol", "TCS")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].stockSymbol", everyItem(is("TCS"))));
        }
    }

    // ===== GET BY ID =====

    @Nested
    @DisplayName("GET /api/transactions/{id}")
    class GetById {

        @Test
        @DisplayName("should return transaction by id")
        void shouldReturnById() throws Exception {
            long id = createBuyTransaction("WIPRO", 50, "450.00", "2023-03-01");

            mockMvc.perform(get(TX_URL + "/" + id)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id))
                    .andExpect(jsonPath("$.stockSymbol").value("WIPRO"));
        }

        @Test
        @DisplayName("should return 404 for non-existent id")
        void shouldReturn404ForNonExistent() throws Exception {
            mockMvc.perform(get(TX_URL + "/999999")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should not allow accessing another user's transaction")
        void shouldNotAccessOtherUserTransaction() throws Exception {
            // Create transaction for user1
            long id = createBuyTransaction("SBIN", 100, "700.00", "2023-04-01");

            // Login as user2
            String user2Token = registerAndGetToken("otheruser@example.com", "password456");

            // user2 tries to access user1's transaction
            mockMvc.perform(get(TX_URL + "/" + id)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + user2Token))
                    .andExpect(status().isNotFound());
        }
    }

    // ===== UPDATE =====

    @Nested
    @DisplayName("PUT /api/transactions/{id}")
    class UpdateTransaction {

        @Test
        @DisplayName("should update transaction notes and price")
        void shouldUpdateTransactionFields() throws Exception {
            long id = createBuyTransaction("HDFC", 20, "1400.00", "2023-05-01");

            UpdateTransactionRequest req = new UpdateTransactionRequest();
            req.setNotes("Updated notes");
            req.setPrice(new BigDecimal("1450.00"));

            mockMvc.perform(put(TX_URL + "/" + id)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.price").value(1450.00))
                    .andExpect(jsonPath("$.notes").value("Updated notes"));
        }

        @Test
        @DisplayName("should return 404 when updating non-existent transaction")
        void shouldReturn404ForNonExistent() throws Exception {
            UpdateTransactionRequest req = new UpdateTransactionRequest();
            req.setNotes("test");

            mockMvc.perform(put(TX_URL + "/999999")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isNotFound());
        }
    }

    // ===== DELETE =====

    @Nested
    @DisplayName("DELETE /api/transactions/{id}")
    class DeleteTransaction {

        @Test
        @DisplayName("should delete transaction and return 204")
        void shouldDeleteTransaction() throws Exception {
            long id = createBuyTransaction("MARUTI", 5, "11000.00", "2023-06-01");

            mockMvc.perform(delete(TX_URL + "/" + id)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isNoContent());

            // Verify it's gone
            mockMvc.perform(get(TX_URL + "/" + id)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should return 404 when deleting non-existent transaction")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            mockMvc.perform(delete(TX_URL + "/999999")
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                    .andExpect(status().isNotFound());
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
                    .lastUpdated(LocalDateTime.now()).build(),
            Stock.builder().symbol("RELIANCE").name("Reliance Industries Ltd")
                    .sector("Conglomerate").currentPrice(new BigDecimal("2500.00"))
                    .lastUpdated(LocalDateTime.now()).build(),
            Stock.builder().symbol("HDFC").name("HDFC Bank Ltd")
                    .sector("Banking").currentPrice(new BigDecimal("1600.00"))
                    .lastUpdated(LocalDateTime.now()).build(),
            Stock.builder().symbol("MARUTI").name("Maruti Suzuki India Ltd")
                    .sector("Automobile").currentPrice(new BigDecimal("12000.00"))
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
        reg.setName("Integration Test User");
        reg.setEmail(email);
        reg.setPassword(password);

        MvcResult result = mockMvc.perform(post(AUTH_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    private long createBuyTransaction(String symbol, int qty,
                                       String price, String date) throws Exception {
        CreateTransactionRequest req = buildBuyRequest(symbol, qty,
                new BigDecimal(price), LocalDate.parse(date));

        MvcResult result = mockMvc.perform(post(TX_URL)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }

    private CreateTransactionRequest buildBuyRequest(String symbol, int qty,
                                                      BigDecimal price, LocalDate date) {
        CreateTransactionRequest req = new CreateTransactionRequest();
        req.setStockSymbol(symbol);
        req.setStockName(symbol + " Ltd");
        req.setType(TransactionType.BUY);
        req.setQuantity(new BigDecimal(qty));
        req.setPrice(price);
        req.setTransactionDate(date);
        req.setBrokerage(new BigDecimal("20.00"));
        return req;
    }
}
