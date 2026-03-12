package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Summary statistics for the portfolio dashboard.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryDTO {

    /** Total amount invested across all current holdings */
    private BigDecimal totalInvested;

    /** Current market value of all holdings */
    private BigDecimal currentValue;

    /** Total unrealised P&L (currentValue - totalInvested) */
    private BigDecimal totalPL;

    /** Overall return percentage */
    private BigDecimal returnPercent;

    /** XIRR – annualised return considering timing of investments */
    private BigDecimal xirr;

    /** Total number of active holdings */
    private Integer totalHoldings;

    /** Total number of transactions */
    private Long totalTransactions;

    /** The holding with the highest return % */
    private HoldingDTO topGainer;

    /** The holding with the lowest (most negative) return % */
    private HoldingDTO topLoser;

    /** The holding with the highest current market value */
    private HoldingDTO largestHolding;

    /** Number of active SIP instructions */
    private Integer activeSipCount;

    /** Total monthly SIP commitment amount */
    private java.math.BigDecimal monthlySipTotal;
}
