package com.niveshtrack.portfolio.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request body for {@code POST /api/auth/refresh}.
 */
@Data
public class RefreshTokenRequest {

    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
