package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.dto.response.StockDTO;
import com.niveshtrack.portfolio.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints for stock master data — search, lookup, and listing.
 * Used by the frontend stock picker for autocomplete.
 */
@RestController
@RequestMapping("/api/stocks")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Stocks", description = "Stock master data & search APIs")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /**
     * Search stocks by symbol or name. Returns up to 10 results.
     * Example: GET /api/stocks/search?q=hd → HDFCBANK, HDFCLIFE, etc.
     */
    @GetMapping("/search")
    @Operation(summary = "Search stocks",
               description = "Case-insensitive search by symbol or name. Returns up to 10 matching stocks.")
    public ResponseEntity<List<StockDTO>> searchStocks(
            @RequestParam(defaultValue = "") String q) {
        return ResponseEntity.ok(stockService.searchStocks(q, 10));
    }

    /**
     * Get a single stock by symbol.
     * Example: GET /api/stocks/RELIANCE
     */
    @GetMapping("/{symbol}")
    @Operation(summary = "Get stock by symbol",
               description = "Returns a single stock's details including current price.")
    public ResponseEntity<StockDTO> getStock(@PathVariable String symbol) {
        return ResponseEntity.ok(stockService.getStock(symbol));
    }

    /**
     * List all stocks in the database.
     * Example: GET /api/stocks
     */
    @GetMapping
    @Operation(summary = "List all stocks",
               description = "Returns all stocks with current prices.")
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        return ResponseEntity.ok(stockService.getAllStocks());
    }
}
