package com.niveshtrack.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the {@code portfolio_snapshots} table.
 * Captures the total portfolio value at the end of each trading day.
 * Used for portfolio growth charts over time.
 */
@Entity
@Table(name = "portfolio_snapshots",
        uniqueConstraints = @UniqueConstraint(name = "uk_snapshot_user_date", columnNames = {"user_id", "snapshot_date"}))
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class PortfolioSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "snapshot_date", nullable = false)
    private LocalDate snapshotDate;

    /** Total current market value of all holdings at snapshot time */
    @Column(name = "total_value", precision = 15, scale = 2)
    private BigDecimal totalValue;

    /** Total amount invested (cost basis) at snapshot time */
    @Column(name = "total_invested", precision = 15, scale = 2)
    private BigDecimal totalInvested;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
