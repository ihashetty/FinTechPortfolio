package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for a mutual fund scheme.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MutualFundDTO {

    private Long id;
    private String symbol;
    private String name;
    private String category;
    private BigDecimal nav;
    private LocalDateTime lastUpdated;
}
