package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * A single item from the user's watchlist, enriched with current price data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistItemDTO {

    private Long id;
    private String stockSymbol;
    private String stockName;
    private String sector;
    private LocalDate addedDate;

    /** Current market price from the stocks table */
    private BigDecimal currentPrice;

    /** Day change amount (placeholder; requires intraday feed) */
    private BigDecimal dayChange;

    /** Day change percentage */
    private BigDecimal dayChangePercent;

    private LocalDateTime createdAt;
}
