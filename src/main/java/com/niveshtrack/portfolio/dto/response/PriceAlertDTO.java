package com.niveshtrack.portfolio.dto.response;

import com.niveshtrack.portfolio.entity.AlertDirection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a price alert.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriceAlertDTO {

    private Long id;
    private String stockSymbol;
    private String stockName;
    private BigDecimal targetPrice;
    private AlertDirection direction;
    private Boolean active;

    /** Current market price (for contextual display) */
    private BigDecimal currentPrice;

    /** Null if not yet triggered */
    private LocalDateTime triggeredAt;
    private LocalDateTime createdAt;
}
