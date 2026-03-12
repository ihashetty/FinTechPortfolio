package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A single data point in the portfolio growth time-series chart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioGrowthDTO {

    /** The date of this data point (end-of-day snapshot date) */
    private LocalDate date;

    /** Portfolio market value on that date */
    private BigDecimal totalValue;

    /** Total amount invested as of that date */
    private BigDecimal totalInvested;

    /** P&L on that date (totalValue - totalInvested) */
    private BigDecimal pnl;

    /** Month label for display, e.g. "Jan 2025" */
    private String monthLabel;
}
