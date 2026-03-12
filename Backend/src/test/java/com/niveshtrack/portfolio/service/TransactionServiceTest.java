package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.request.CreateTransactionRequest;
import com.niveshtrack.portfolio.dto.request.UpdateTransactionRequest;
import com.niveshtrack.portfolio.dto.response.TransactionDTO;
import com.niveshtrack.portfolio.entity.Transaction;
import com.niveshtrack.portfolio.entity.TransactionType;
import com.niveshtrack.portfolio.entity.User;
import com.niveshtrack.portfolio.exception.ResourceNotFoundException;
import com.niveshtrack.portfolio.repository.AssetPriceHistoryRepository;
import com.niveshtrack.portfolio.repository.StockRepository;
import com.niveshtrack.portfolio.repository.TransactionRepository;
import com.niveshtrack.portfolio.repository.UserRepository;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private HoldingsService holdingsService;

    @Mock
    private WalletService walletService;

    @Mock
    private HoldingService holdingService;

    @Mock
    private AssetPriceHistoryRepository priceHistoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User demoUser;
    private Transaction sampleBuy;

    @BeforeEach
    void setUp() {
        demoUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .passwordHash("hashed")
                .currency("INR")
                .build();

        sampleBuy = Transaction.builder()
                .id(10L)
                .user(demoUser)
                .stockSymbol("TCS")
                .stockName("Tata Consultancy Services Ltd")
                .type(TransactionType.BUY)
                .quantity(new BigDecimal("20"))
                .price(new BigDecimal("3200.00"))
                .transactionDate(LocalDate.of(2023, 6, 1))
                .brokerage(new BigDecimal("20.00"))
                .notes("Test buy")
                .build();
    }

    // ===== GET ALL =====

    @Nested
    @DisplayName("Get Transactions")
    class GetTransactions {

        @Test
        @DisplayName("should return all transactions for a user")
        void shouldReturnAllTransactionsForUser() {
            Transaction tx2 = Transaction.builder()
                    .id(11L).user(demoUser).stockSymbol("INFY").stockName("Infosys Ltd")
                    .type(TransactionType.BUY).quantity(new BigDecimal("30")).price(new BigDecimal("1500.00"))
                    .transactionDate(LocalDate.of(2023, 7, 1)).brokerage(BigDecimal.ZERO).build();

            given(transactionRepository.findByUserIdOrderByTransactionDateDesc(1L))
                    .willReturn(Arrays.asList(sampleBuy, tx2));

            List<TransactionDTO> result = transactionService.getAllTransactions(1L, null);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getStockSymbol()).isEqualTo("TCS");
            assertThat(result.get(1).getStockSymbol()).isEqualTo("INFY");
        }

        @Test
        @DisplayName("should return empty list when user has no transactions")
        void shouldReturnEmptyListForNewUser() {
            given(transactionRepository.findByUserIdOrderByTransactionDateDesc(99L))
                    .willReturn(List.of());

            List<TransactionDTO> result = transactionService.getAllTransactions(99L, null);

            assertThat(result).isEmpty();
        }
    }

    // ===== GET BY ID =====

    @Nested
    @DisplayName("Get Transaction By ID")
    class GetTransactionById {

        @Test
        @DisplayName("should return transaction when found")
        void shouldReturnTransactionWhenFound() {
            given(transactionRepository.findByIdAndUserId(10L, 1L))
                    .willReturn(Optional.of(sampleBuy));

            TransactionDTO result = transactionService.getTransactionById(1L, 10L);

            assertThat(result.getId()).isEqualTo(10L);
            assertThat(result.getStockSymbol()).isEqualTo("TCS");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            given(transactionRepository.findByIdAndUserId(99L, 1L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.getTransactionById(1L, 99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Transaction");
        }
    }

    // ===== UPDATE =====

    @Nested
    @DisplayName("Update Transaction")
    class UpdateTransaction {

        @Test
        @DisplayName("should partially update a transaction")
        void shouldPartiallyUpdateTransaction() {
            given(transactionRepository.findByIdAndUserId(10L, 1L))
                    .willReturn(Optional.of(sampleBuy));

            UpdateTransactionRequest req = new UpdateTransactionRequest();
            req.setNotes("Updated notes");
            req.setPrice(new BigDecimal("3500.00"));

            Transaction updated = Transaction.builder()
                    .id(10L).user(demoUser).stockSymbol("TCS")
                    .stockName("Tata Consultancy Services Ltd").type(TransactionType.BUY)
                    .quantity(new BigDecimal("20")).price(new BigDecimal("3500.00"))
                    .transactionDate(LocalDate.of(2023, 6, 1)).notes("Updated notes").build();

            given(transactionRepository.save(any(Transaction.class))).willReturn(updated);

            TransactionDTO result = transactionService.updateTransaction(1L, 10L, req);

            assertThat(result.getPrice()).isEqualByComparingTo("3500.00");
            verify(holdingsService).evictHoldingsCache(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when updating non-existent transaction")
        void shouldThrowWhenUpdatingNonExistent() {
            given(transactionRepository.findByIdAndUserId(999L, 1L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.updateTransaction(1L, 999L, new UpdateTransactionRequest()))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ===== DELETE =====

    @Nested
    @DisplayName("Delete Transaction")
    class DeleteTransaction {

        @Test
        @DisplayName("should delete an existing transaction")
        void shouldDeleteTransaction() {
            given(transactionRepository.findByIdAndUserId(10L, 1L))
                    .willReturn(Optional.of(sampleBuy));

            transactionService.deleteTransaction(1L, 10L);

            verify(transactionRepository).delete(sampleBuy);
            verify(holdingsService).evictHoldingsCache(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when deleting non-existent transaction")
        void shouldThrowWhenDeletingNonExistent() {
            given(transactionRepository.findByIdAndUserId(999L, 1L))
                    .willReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.deleteTransaction(1L, 999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(transactionRepository, never()).delete(any());
        }
    }
}
