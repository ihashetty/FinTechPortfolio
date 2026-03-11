package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.dto.response.DashboardSummaryDTO;
import com.niveshtrack.portfolio.dto.response.PortfolioAllocationDTO;
import com.niveshtrack.portfolio.dto.response.PortfolioGrowthDTO;
import com.niveshtrack.portfolio.security.UserDetailsServiceImpl;
import com.niveshtrack.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Portfolio-level endpoints: dashboard summary, sector allocation, growth chart.
 */
@RestController
@RequestMapping("/api/portfolio")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Portfolio", description = "Portfolio analytics – dashboard, allocation, and growth chart APIs")
public class PortfolioController extends BaseController {

    private final PortfolioService portfolioService;

    public PortfolioController(UserDetailsServiceImpl userDetailsService,
                                PortfolioService portfolioService) {
        super(userDetailsService);
        this.portfolioService = portfolioService;
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard summary",
               description = "Returns totalInvested, currentValue, P&L, XIRR, top gainer/loser, and largest holding.")
    public ResponseEntity<DashboardSummaryDTO> getDashboard() {
        return ResponseEntity.ok(portfolioService.getDashboardSummary(getCurrentUserId()));
    }

    @GetMapping("/allocation")
    @Operation(summary = "Get sector allocation",
               description = "Returns portfolio value and percentage broken down by sector.")
    public ResponseEntity<List<PortfolioAllocationDTO>> getAllocation() {
        return ResponseEntity.ok(portfolioService.getPortfolioAllocation(getCurrentUserId()));
    }

    @GetMapping("/growth")
    @Operation(summary = "Get portfolio growth (last 13 months)",
               description = "Returns monthly portfolio value data points for time-series chart.")
    public ResponseEntity<List<PortfolioGrowthDTO>> getGrowth() {
        return ResponseEntity.ok(portfolioService.getPortfolioGrowth(getCurrentUserId()));
    }
}
