package com.niveshtrack.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the {@code account_ledger} table.
 * Tracks all wallet credits and debits using double-entry style.
 *
 * <p>Balance = SUM(amount) for a given user.
 * Positive amounts are credits (DEPOSIT, SELL proceeds).
 * Negative amounts are debits (WITHDRAW, BUY cost, BROKERAGE).
 */
@Entity
@Table(name = "account_ledger", indexes = {
        @Index(name = "idx_ledger_user", columnList = "user_id")
})
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class AccountLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private LedgerType type;

    /**
     * Positive = credit (deposit, sell proceeds).
     * Negative = debit (withdraw, buy cost, brokerage).
     */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /** Reference to the transaction ID that caused this ledger entry (nullable for DEPOSIT/WITHDRAW). */
    @Column(name = "reference_id")
    private Long referenceId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime createdAt;
}
