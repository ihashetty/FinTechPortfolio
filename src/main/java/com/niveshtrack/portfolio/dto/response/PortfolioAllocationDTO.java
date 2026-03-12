package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Portfolio allocation broken down by sector.
 * Used to render pie/donut charts on the frontend.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioAllocationDTO {

    /** Sector name, e.g. "Information Technology", "Banking" */
    private String sector;

    /** Total current market value of holdings in this sector */
    private BigDecimal totalValue;

    /** Percentage of the total portfolio this sector represents */
    private BigDecimal percentage;

    /** Number of distinct stocks held in this sector */
    private Integer stockCount;
}
