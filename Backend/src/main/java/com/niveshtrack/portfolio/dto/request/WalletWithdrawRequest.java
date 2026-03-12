package com.niveshtrack.portfolio.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for {@code POST /api/wallet/withdraw}.
 */
@Data
public class WalletWithdrawRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Withdrawal amount must be positive")
    @DecimalMin(value = "1.00", message = "Minimum withdrawal is ₹1.00")
    private BigDecimal amount;
}
