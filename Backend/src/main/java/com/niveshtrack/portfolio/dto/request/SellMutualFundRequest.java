package com.niveshtrack.portfolio.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /api/mutual-funds/sell}.
 */
@Data
public class SellMutualFundRequest {

    @NotBlank(message = "Fund symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String symbol;

    @NotNull(message = "Units is required")
    @Positive(message = "Units must be positive")
    @DecimalMin(value = "0.0001", message = "Minimum units is 0.0001")
    private BigDecimal units;
}
