package com.niveshtrack.portfolio.controller;

import com.niveshtrack.portfolio.dto.request.AddWatchlistRequest;
import com.niveshtrack.portfolio.dto.response.WatchlistItemDTO;
import com.niveshtrack.portfolio.security.UserDetailsServiceImpl;
import com.niveshtrack.portfolio.service.WatchlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Manages the user's stock watchlist.
 */
@RestController
@RequestMapping("/api/watchlist")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Watchlist", description = "Stock watchlist management APIs")
public class WatchlistController extends BaseController {

    private final WatchlistService watchlistService;

    public WatchlistController(UserDetailsServiceImpl userDetailsService,
                                WatchlistService watchlistService) {
        super(userDetailsService);
        this.watchlistService = watchlistService;
    }

    @GetMapping
    @Operation(summary = "Get watchlist",
               description = "Returns all stocks in the user's watchlist enriched with current price data.")
    public ResponseEntity<List<WatchlistItemDTO>> getWatchlist() {
        return ResponseEntity.ok(watchlistService.getWatchlist(getCurrentUserId()));
    }

    @PostMapping
    @Operation(summary = "Add stock to watchlist")
    public ResponseEntity<WatchlistItemDTO> addToWatchlist(
            @Valid @RequestBody AddWatchlistRequest request) {
        WatchlistItemDTO item = watchlistService.addToWatchlist(getCurrentUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove stock from watchlist")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable Long id) {
        watchlistService.removeFromWatchlist(getCurrentUserId(), id);
        return ResponseEntity.noContent().build();
    }
}
