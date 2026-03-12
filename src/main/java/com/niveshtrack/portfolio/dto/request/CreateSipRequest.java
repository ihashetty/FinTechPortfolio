package com.niveshtrack.portfolio.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /api/sip}.
 */
@Data
public class CreateSipRequest {

    @NotBlank(message = "Fund symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    private String symbol;

    @NotNull(message = "SIP amount is required")
    @Positive(message = "SIP amount must be positive")
    @DecimalMin(value = "100.00", message = "Minimum SIP amount is ₹100.00")
    private BigDecimal amount;

    /** Frequency of SIP execution. Defaults to MONTHLY if not provided. */
    private String frequency;
}
