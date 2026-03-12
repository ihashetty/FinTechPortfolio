package com.niveshtrack.portfolio.dto.request;

import com.niveshtrack.portfolio.entity.AssetType;
import com.niveshtrack.portfolio.entity.TransactionType;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request body for {@code PUT /api/transactions/{id}}.
 * All fields are optional — only non-null fields will be updated.
 */
@Data
public class UpdateTransactionRequest {

    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String stockSymbol;

    @Size(max = 200, message = "Stock name must not exceed 200 characters")
    private String stockName;

    private TransactionType type;

    private AssetType assetType;

    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @Positive(message = "Price must be positive")
    @DecimalMin(value = "0.01", message = "Price must be at least ₹0.01")
    private BigDecimal price;

    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDate transactionDate;

    @DecimalMin(value = "0.0", message = "Brokerage cannot be negative")
    private BigDecimal brokerage;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
