package com.niveshtrack.portfolio.repository;

import com.niveshtrack.portfolio.entity.AccountLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for wallet ledger operations.
 */
@Repository
public interface AccountLedgerRepository extends JpaRepository<AccountLedger, Long> {

    List<AccountLedger> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Calculates the current wallet balance as SUM of all ledger amounts.
     * Positive entries (DEPOSIT, SELL) add to balance.
     * Negative entries (WITHDRAW, BUY, BROKERAGE) reduce balance.
     */
    @Query("SELECT COALESCE(SUM(a.amount), 0) FROM AccountLedger a WHERE a.user.id = :userId")
    BigDecimal getBalance(@Param("userId") Long userId);

    List<AccountLedger> findByUserIdAndReferenceIdOrderByCreatedAtDesc(Long userId, Long referenceId);
}
