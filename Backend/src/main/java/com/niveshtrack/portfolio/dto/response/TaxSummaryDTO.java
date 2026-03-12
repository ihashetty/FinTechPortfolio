package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Tax summary for a given Indian financial year (e.g., "2024-25").
 *
 * <p>Indian Tax Rules (Equity):
 * <ul>
 *   <li>STCG: Holding period &lt; 365 days → taxed at 20% (revised from July 2024)</li>
 *   <li>LTCG: Holding period ≥ 365 days → 12.5% on gains exceeding ₹1.25 lakh (revised from July 2024)</li>
 *   <li>LTCG exemption: ₹1,25,000 per financial year (revised limit)</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxSummaryDTO {

    private String financialYear;          // e.g. "2024-25"

    // ===== STCG =====
    private BigDecimal totalStcgGains;     // Total short-term gains
    private BigDecimal totalStcgLosses;    // Total short-term losses
    private BigDecimal netStcg;            // Net STCG (gains - losses)
    private BigDecimal stcgTaxRate;        // e.g. 20.0
    private BigDecimal stcgTaxLiability;   // Tax payable on net STCG

    // ===== LTCG =====
    private BigDecimal totalLtcgGains;     // Total long-term gains
    private BigDecimal totalLtcgLosses;    // Total long-term losses
    private BigDecimal netLtcg;            // Net LTCG (gains - losses)
    private BigDecimal ltcgExemption;      // ₹1,25,000 exemption
    private BigDecimal taxableLtcg;        // Net LTCG above exemption
    private BigDecimal ltcgTaxRate;        // e.g. 12.5
    private BigDecimal ltcgTaxLiability;   // Tax payable on taxable LTCG

    // ===== Total =====
    private BigDecimal totalTaxLiability;  // STCG + LTCG tax
    private BigDecimal totalRealised;      // Total realised gain/loss

    /** Individual transaction-level breakdown */
    private List<TaxLineItem> lineItems;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaxLineItem {
        private String stockSymbol;
        private String stockName;
        private Integer quantity;
        private BigDecimal buyPrice;
        private BigDecimal sellPrice;
        private String buyDate;
        private String sellDate;
        private Integer holdingDays;
        private String taxType;            // "STCG" or "LTCG"
        private BigDecimal gain;
        private BigDecimal taxLiability;
    }
}
