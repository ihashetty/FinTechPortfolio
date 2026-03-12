package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Monthly profit & loss summary — both realised and unrealised.
 * Used for the monthly P&L bar chart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPLDTO {

    /** e.g. "2025-01" */
    private String month;

    /** Human-readable label, e.g. "Jan 2025" */
    private String monthLabel;

    /** Year number */
    private Integer year;

    /** Month number (1–12) */
    private Integer monthNumber;

    /** Realised P&L from completed SELL transactions this month */
    private BigDecimal realisedPL;

    /** Unrealised P&L as of end-of-month (based on snapshot if available) */
    private BigDecimal unrealisedPL;

    /** Total invested during this month (sum of BUY amounts) */
    private BigDecimal invested;

    /** Total proceeds from SELL transactions this month */
    private BigDecimal proceeds;
}
