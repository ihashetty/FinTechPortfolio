package com.niveshtrack.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the {@code mutual_funds} table.
 * Master table for available mutual fund schemes.
 *
 * <p>Pre-seeded with demo funds. NAV is updated daily by the scheduler.
 */
@Entity
@Table(name = "mutual_funds")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MutualFund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "symbol", nullable = false, unique = true, length = 20)
    private String symbol;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "nav", precision = 15, scale = 4)
    private BigDecimal nav;

    @Column(name = "last_updated", columnDefinition = "DATETIME(6)")
    private LocalDateTime lastUpdated;
}
