package com.niveshtrack.portfolio.service;

import com.niveshtrack.portfolio.dto.response.DashboardSummaryDTO;
import com.niveshtrack.portfolio.dto.response.HoldingDTO;
import com.niveshtrack.portfolio.dto.response.MonthlyPLDTO;
import com.niveshtrack.portfolio.dto.response.TaxSummaryDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Generates downloadable reports (portfolio summary, transactions, tax).
 *
 * <p>Current implementation returns CSV-formatted byte arrays.
 * For PDF/Excel, integrate Apache POI (Excel) or OpenPDF (PDF).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final HoldingsService holdingsService;
    private final PortfolioService portfolioService;
    private final AnalyticsService analyticsService;

    /**
     * Generates a portfolio summary report in CSV format.
     */
    @Transactional(readOnly = true)
    public byte[] generatePortfolioSummaryReport(Long userId, String format) {
        DashboardSummaryDTO summary = portfolioService.getDashboardSummary(userId);
        List<HoldingDTO> holdings = holdingsService.calculateHoldings(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("Portfolio Summary Report\n\n");
        sb.append(String.format("Total Invested,%.2f%n", summary.getTotalInvested()));
        sb.append(String.format("Current Value,%.2f%n", summary.getCurrentValue()));
        sb.append(String.format("Total P&L,%.2f%n", summary.getTotalPL()));
        sb.append(String.format("Return %%,%.2f%n", summary.getReturnPercent()));
        sb.append(String.format("XIRR (%%),%.2f%n",
                summary.getXirr() != null ? summary.getXirr() : BigDecimal.ZERO));
        sb.append("\nHoldings\n");
        sb.append("Symbol,Name,Sector,Qty,Avg Buy Price,Current Price,Invested,Value,Gain/Loss,Return %%\n");

        for (HoldingDTO h : holdings) {
            sb.append(String.format("%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f%n",
                    h.getSymbol(), nvl(h.getName()), nvl(h.getSector()),
                    h.getQuantity().toPlainString(), h.getAvgBuyPrice(), h.getCurrentPrice(),
                    h.getInvestedAmount(), h.getTotalValue(), h.getGainLoss(), h.getReturnPercent()));
        }

        log.info("Generated portfolio summary report for userId={}", userId);
        return sb.toString().getBytes();
    }

    /**
     * Generates a tax report for a financial year in CSV format.
     */
    @Transactional(readOnly = true)
    public byte[] generateTaxReport(Long userId, String financialYear, String format) {
        TaxSummaryDTO tax = analyticsService.getTaxSummary(userId, financialYear);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Tax Summary – FY %s%n%n", tax.getFinancialYear()));
        sb.append(String.format("Net STCG,%.2f%n", tax.getNetStcg()));
        sb.append(String.format("STCG Tax (@%s%%),%.2f%n", tax.getStcgTaxRate(), tax.getStcgTaxLiability()));
        sb.append(String.format("Net LTCG,%.2f%n", tax.getNetLtcg()));
        sb.append(String.format("LTCG Exemption,%.2f%n", tax.getLtcgExemption()));
        sb.append(String.format("Taxable LTCG,%.2f%n", tax.getTaxableLtcg()));
        sb.append(String.format("LTCG Tax (@%s%%),%.2f%n", tax.getLtcgTaxRate(), tax.getLtcgTaxLiability()));
        sb.append(String.format("Total Tax Liability,%.2f%n%n", tax.getTotalTaxLiability()));

        sb.append("Detailed Transactions\n");
        sb.append("Symbol,Qty,Buy Price,Sell Price,Buy Date,Sell Date,Holding Days,Type,Gain/Loss\n");

        if (tax.getLineItems() != null) {
            for (TaxSummaryDTO.TaxLineItem item : tax.getLineItems()) {
                sb.append(String.format("%s,%d,%.2f,%.2f,%s,%s,%d,%s,%.2f%n",
                        item.getStockSymbol(), item.getQuantity(),
                        item.getBuyPrice(), item.getSellPrice(),
                        item.getBuyDate(), item.getSellDate(),
                        item.getHoldingDays(), item.getTaxType(), item.getGain()));
            }
        }

        log.info("Generated tax report for userId={}, fy={}", userId, financialYear);
        return sb.toString().getBytes();
    }

    /**
     * Generates a monthly P&L report in CSV format.
     */
    @Transactional(readOnly = true)
    public byte[] generateTransactionsReport(Long userId, String format) {
        List<MonthlyPLDTO> monthlyPL = analyticsService.getMonthlyPL(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("Monthly P&L Report\n\n");
        sb.append("Month,Invested,Proceeds,Realised P&L\n");

        for (MonthlyPLDTO dto : monthlyPL) {
            sb.append(String.format("%s,%.2f,%.2f,%.2f%n",
                    dto.getMonthLabel(),
                    dto.getInvested(),
                    dto.getProceeds(),
                    dto.getRealisedPL()));
        }

        return sb.toString().getBytes();
    }

    private String nvl(String val) {
        return val != null ? val : "";
    }
}
