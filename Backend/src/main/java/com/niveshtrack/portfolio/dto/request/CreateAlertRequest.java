package com.niveshtrack.portfolio.dto.request;

import com.niveshtrack.portfolio.entity.AlertDirection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /api/alerts}.
 */
@Data
public class CreateAlertRequest {

    @NotBlank(message = "Stock symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String stockSymbol;

    @Size(max = 200, message = "Stock name must not exceed 200 characters")
    private String stockName;

    @NotNull(message = "Target price is required")
    @DecimalMin(value = "0.01", message = "Target price must be greater than 0")
    private BigDecimal targetPrice;

    @NotNull(message = "Direction is required (ABOVE or BELOW)")
    private AlertDirection direction;
}
