package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.dto.response.MonthlyPLDTO;
import com.niveshtrack.portfolio.dto.response.PortfolioAllocationDTO;
import com.niveshtrack.portfolio.dto.response.TaxSummaryDTO;
import com.niveshtrack.portfolio.security.UserDetailsServiceImpl;
import com.niveshtrack.portfolio.service.AnalyticsService;
import com.niveshtrack.portfolio.util.DateUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Portfolio analytics endpoints: monthly P&L, sector allocation, tax summary.
 */
@RestController
@RequestMapping("/api/analytics")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Analytics", description = "Portfolio analytics APIs – monthly P&L, tax summary, sector allocation")
public class AnalyticsController extends BaseController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(UserDetailsServiceImpl userDetailsService,
                                AnalyticsService analyticsService) {
        super(userDetailsService);
        this.analyticsService = analyticsService;
    }

    @GetMapping("/monthly-pl")
    @Operation(summary = "Get monthly P&L",
               description = "Returns monthly realised P&L for the last 12 months.")
    public ResponseEntity<List<MonthlyPLDTO>> getMonthlyPL() {
        return ResponseEntity.ok(analyticsService.getMonthlyPL(getCurrentUserId()));
    }

    @GetMapping("/sector")
    @Operation(summary = "Get sector allocation",
               description = "Returns portfolio breakdown by sector with value and percentage.")
    public ResponseEntity<List<PortfolioAllocationDTO>> getSectorAllocation() {
        return ResponseEntity.ok(analyticsService.getSectorAllocation(getCurrentUserId()));
    }

    @GetMapping("/tax-summary")
    @Operation(summary = "Get STCG/LTCG tax summary",
               description = "Calculates Indian capital gains tax for the specified financial year (e.g., '2024-25').")
    public ResponseEntity<TaxSummaryDTO> getTaxSummary(
            @RequestParam(required = false, defaultValue = "") String fy) {
        String financialYear = fy.isBlank() ? DateUtils.currentFinancialYear() : fy;
        return ResponseEntity.ok(analyticsService.getTaxSummary(getCurrentUserId(), financialYear));
    }
}
