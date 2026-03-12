package com.niveshtrack.portfolio.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /api/mutual-funds/buy}.
 */
@Data
public class BuyMutualFundRequest {

    @NotBlank(message = "Fund symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String symbol;

    @NotNull(message = "Amount is required")
    @Positive(message = "Investment amount must be positive")
    @DecimalMin(value = "100.00", message = "Minimum investment is ₹100.00")
    private BigDecimal amount;
}
