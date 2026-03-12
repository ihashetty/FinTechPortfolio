package com.niveshtrack.portfolio.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /api/wallet/deposit}.
 */
@Data
public class WalletDepositRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Deposit amount must be positive")
    @DecimalMin(value = "1.00", message = "Minimum deposit is ₹1.00")
    private BigDecimal amount;
}
