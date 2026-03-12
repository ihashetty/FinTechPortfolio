package com.niveshtrack.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the {@code holdings} table.
 * Stores the current position for each user-asset combination.
 *
 * <p>Updated after every BUY/SELL transaction. Uses optimistic locking
 * via {@code @Version} to handle concurrent modifications safely.
 */
@Entity
@Table(name = "holdings",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_asset", columnNames = {"user_id", "asset_type", "symbol"}),
        indexes = @Index(name = "idx_holdings_user", columnList = "user_id")
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = "user")
@ToString(exclude = "user")
public class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    private AssetType assetType;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "quantity", nullable = false, precision = 15, scale = 4)
    private BigDecimal quantity;

    @Column(name = "average_price", nullable = false, precision = 15, scale = 4)
    private BigDecimal averagePrice;

    @Column(name = "last_updated", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime lastUpdated;

    /** Optimistic locking to prevent concurrent modification issues. */
    @Version
    @Column(name = "version")
    private Long version;
}
