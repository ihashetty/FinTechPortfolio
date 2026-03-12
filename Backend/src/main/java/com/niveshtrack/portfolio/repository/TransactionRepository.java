package com.niveshtrack.portfolio.repository;

import com.niveshtrack.portfolio.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Transaction} entities.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** All transactions for a user ordered by date descending */
    List<Transaction> findByUserIdOrderByTransactionDateDesc(Long userId);

    /** Transactions filtered by user and stock symbol */
    List<Transaction> findByUserIdAndStockSymbol(Long userId, String stockSymbol);

    /**
     * Transactions for a specific user and stock ordered by date ascending
     * (needed for FIFO/LIFO calculations).
     */
    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId AND t.stockSymbol = :symbol ORDER BY t.transactionDate ASC")
    List<Transaction> findTransactionsByUserAndSymbol(@Param("userId") Long userId, @Param("symbol") String symbol);

    /** Distinct stock symbols present in a user's portfolio */
    @Query("SELECT DISTINCT t.stockSymbol FROM Transaction t WHERE t.user.id = :userId")
    List<String> findDistinctStockSymbolsByUserId(@Param("userId") Long userId);

    /** Find transaction by ID AND userId (ownership verification) */
    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    /** Transactions within a date range for a user (used for analytics) */
    List<Transaction> findByUserIdAndTransactionDateBetweenOrderByTransactionDateAsc(
            Long userId, LocalDate from, LocalDate to);

    /** Count of transactions for a user (dashboard stat) */
    long countByUserId(Long userId);
}
