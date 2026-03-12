package com.niveshtrack.portfolio.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity mapping to the {@code stocks} table.
 * Stores master data and current prices for NSE/BSE traded stocks.
 */
@Entity
@Table(name = "stocks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Stock {

    /** NSE/BSE ticker symbol, e.g. "RELIANCE", "TCS", "INFY" */
    @Id
    @Column(name = "symbol", length = 20)
    private String symbol;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "sector", length = 100)
    private String sector;

    @Column(name = "current_price", precision = 12, scale = 4)
    private BigDecimal currentPrice;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
