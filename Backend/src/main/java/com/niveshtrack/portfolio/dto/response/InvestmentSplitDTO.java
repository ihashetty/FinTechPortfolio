package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Breakdown of investment by mode: SIP vs Lumpsum (one-time).
 * Used for the SIP vs Lumpsum pie chart on the dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvestmentSplitDTO {

    /** Total amount invested via SIP */
    private BigDecimal sipAmount;

    /** Total amount invested via one-time (lumpsum) purchases */
    private BigDecimal lumpsumAmount;

    /** SIP percentage of total MF investment */
    private BigDecimal sipPercent;

    /** Lumpsum percentage of total MF investment */
    private BigDecimal lumpsumPercent;
}
