package com.niveshtrack.portfolio.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for {@code POST /api/watchlist}.
 */
@Data
public class AddWatchlistRequest {

    @NotBlank(message = "Stock symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String stockSymbol;

    @Size(max = 200, message = "Stock name must not exceed 200 characters")
    private String stockName;
}
