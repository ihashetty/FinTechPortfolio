package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.dto.response.HoldingDTO;
import com.niveshtrack.portfolio.security.UserDetailsServiceImpl;
import com.niveshtrack.portfolio.service.HoldingsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Returns current portfolio holdings calculated in real-time from transactions.
 */
@RestController
@RequestMapping("/api/holdings")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Holdings", description = "Current portfolio holdings calculated from transactions")
public class HoldingsController extends BaseController {

    private final HoldingsService holdingsService;

    public HoldingsController(UserDetailsServiceImpl userDetailsService,
                               HoldingsService holdingsService) {
        super(userDetailsService);
        this.holdingsService = holdingsService;
    }

    @GetMapping
    @Operation(summary = "Get current holdings",
               description = "Returns all active stock holdings with weighted average cost, P&L, and sector allocation.")
    public ResponseEntity<List<HoldingDTO>> getHoldings() {
        return ResponseEntity.ok(holdingsService.calculateHoldings(getCurrentUserId()));
    }
}
