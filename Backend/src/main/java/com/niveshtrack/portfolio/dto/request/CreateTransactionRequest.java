package com.niveshtrack.portfolio.dto.request;

import com.niveshtrack.portfolio.entity.AssetType;
import com.niveshtrack.portfolio.entity.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for {@code POST /api/transactions}.
 *
 * <p>If {@code orderType} is {@code MARKET} (the default), the {@code price} field
 * can be omitted — the backend will resolve the current market price from the
 * {@code stocks} table. If {@code orderType} is {@code LIMIT}, the client must
 * supply a positive {@code price}.
 */
@Data
public class CreateTransactionRequest {

    /** Asset type: STOCK or MF. Defaults to STOCK if not provided. */
    private AssetType assetType;

    @NotBlank(message = "Stock symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String stockSymbol;

    @Size(max = 200, message = "Stock name must not exceed 200 characters")
    private String stockName;

    @NotNull(message = "Transaction type is required (BUY or SELL)")
    private TransactionType type;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    /**
     * Price per share. Required for LIMIT orders; optional for MARKET orders.
     * When null and orderType is MARKET, the backend uses the current price
     * from the stocks table.
     */
    @DecimalMin(value = "0.01", message = "Price must be at least ₹0.01")
    private BigDecimal price;

    /**
     * Order type: MARKET or LIMIT. Defaults to MARKET if not provided.
     * MARKET = execute at current market price; LIMIT = execute at user-specified price.
     */
    private String orderType;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate transactionDate;

    @DecimalMin(value = "0.0", message = "Brokerage cannot be negative")
    private BigDecimal brokerage;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
