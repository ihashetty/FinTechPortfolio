package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.dto.request.BuyMutualFundRequest;
import com.niveshtrack.portfolio.dto.request.SellMutualFundRequest;
import com.niveshtrack.portfolio.dto.response.HoldingDTO;
import com.niveshtrack.portfolio.dto.response.MutualFundDTO;
import com.niveshtrack.portfolio.entity.AssetType;
import com.niveshtrack.portfolio.security.UserDetailsServiceImpl;
import com.niveshtrack.portfolio.service.HoldingService;
import com.niveshtrack.portfolio.service.MutualFundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Mutual fund browsing, one-time buy/sell endpoints.
 */
@RestController
@RequestMapping("/api/mutual-funds")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Mutual Funds", description = "Browse, buy, and sell mutual fund units")
public class MutualFundController extends BaseController {

    private final MutualFundService mutualFundService;
    private final HoldingService holdingService;

    public MutualFundController(UserDetailsServiceImpl userDetailsService,
                                MutualFundService mutualFundService,
                                HoldingService holdingService) {
        super(userDetailsService);
        this.mutualFundService = mutualFundService;
        this.holdingService = holdingService;
    }

    @GetMapping
    @Operation(summary = "List all mutual funds",
               description = "Returns all available mutual funds with latest NAV.")
    public ResponseEntity<List<MutualFundDTO>> getAllFunds() {
        return ResponseEntity.ok(mutualFundService.getAllFunds());
    }

    @PostMapping("/buy")
    @Operation(summary = "Buy mutual fund units (one-time)",
               description = "Purchases mutual fund units for the given amount. Units = amount / NAV.")
    public ResponseEntity<String> buy(@Valid @RequestBody BuyMutualFundRequest request) {
        mutualFundService.buyOneTime(getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Mutual fund purchase successful for " + request.getSymbol());
    }

    @PostMapping("/sell")
    @Operation(summary = "Sell mutual fund units",
               description = "Sells the specified number of MF units. Proceeds are credited to wallet.")
    public ResponseEntity<String> sell(@Valid @RequestBody SellMutualFundRequest request) {
        mutualFundService.sellUnits(getCurrentUserId(), request);
        return ResponseEntity.ok("Mutual fund sell successful for " + request.getSymbol());
    }

    @GetMapping("/holdings")
    @Operation(summary = "Get mutual fund holdings",
               description = "Returns all MF holdings for the authenticated user.")
    public ResponseEntity<List<HoldingDTO>> getMfHoldings() {
        return ResponseEntity.ok(
                holdingService.getUserHoldingsByAssetType(getCurrentUserId(), AssetType.MF));
    }
}
