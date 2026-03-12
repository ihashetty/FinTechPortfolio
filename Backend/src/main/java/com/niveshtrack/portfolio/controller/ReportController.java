package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.security.UserDetailsServiceImpl;
import com.niveshtrack.portfolio.service.ReportService;
import com.niveshtrack.portfolio.util.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Generates downloadable reports (portfolio summary, transactions, tax).
 * Returns CSV files by default.
 */
@RestController
@RequestMapping("/api/reports")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Reports", description = "Downloadable report generation APIs")
public class ReportController extends BaseController {

    private final ReportService reportService;

    public ReportController(UserDetailsServiceImpl userDetailsService,
                             ReportService reportService) {
        super(userDetailsService);
        this.reportService = reportService;
    }

    @GetMapping("/portfolio-summary")
    @Operation(summary = "Download portfolio summary report",
               description = "Generates a CSV summary of all current holdings and portfolio metrics.")
    public ResponseEntity<byte[]> getPortfolioSummaryReport(
            @RequestParam(required = false, defaultValue = "csv") String format) {

        byte[] data = reportService.generatePortfolioSummaryReport(getCurrentUserId(), format);
        return buildDownloadResponse(data, "portfolio-summary.csv");
    }

    @GetMapping("/transactions")
    @Operation(summary = "Download transactions / monthly P&L report",
               description = "Generates a CSV monthly P&L report.")
    public ResponseEntity<byte[]> getTransactionsReport(
            @RequestParam(required = false, defaultValue = "csv") String format) {

        byte[] data = reportService.generateTransactionsReport(getCurrentUserId(), format);
        return buildDownloadResponse(data, "monthly-pl.csv");
    }

    @GetMapping("/tax-report")
    @Operation(summary = "Download STCG/LTCG tax report",
               description = "Generates a CSV capital gains tax report for the specified Indian financial year.")
    public ResponseEntity<byte[]> getTaxReport(
            @RequestParam(required = false, defaultValue = "") String fy,
            @RequestParam(required = false, defaultValue = "csv") String format) {

        String financialYear = fy.isBlank() ? DateUtils.currentFinancialYear() : fy;
        byte[] data = reportService.generateTaxReport(getCurrentUserId(), financialYear, format);
        return buildDownloadResponse(data, "tax-report-" + financialYear + ".csv");
    }

    private ResponseEntity<byte[]> buildDownloadResponse(byte[] data, String filename) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(data.length)
                .body(data);
    }
}
