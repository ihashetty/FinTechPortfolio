package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for stock master data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDTO {

    private String symbol;
    private String name;
    private String sector;
    private BigDecimal currentPrice;
    private LocalDateTime lastUpdated;
}
