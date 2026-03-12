package com.niveshtrack.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the {@code asset_price_history} table.
 * Stores historical price data for both stocks and mutual funds.
 *
 * <p>Powers: growth charts, historical comparison, simulation tracking.
 */
@Entity
@Table(name = "asset_price_history", indexes = {
        @Index(name = "idx_symbol_time", columnList = "symbol, recorded_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetPriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false, length = 20)
    private AssetType assetType;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "price", nullable = false, precision = 15, scale = 4)
    private BigDecimal price;

    @Column(name = "recorded_at", nullable = false, columnDefinition = "DATETIME(6)")
    private LocalDateTime recordedAt;

    /** Source of the price data: ALPHA_VANTAGE, SIMULATED, NAV_UPDATE */
    @Column(name = "source", nullable = false, length = 20)
    private String source;
}
