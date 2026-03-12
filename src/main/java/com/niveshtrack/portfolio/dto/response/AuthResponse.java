package com.niveshtrack.portfolio.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for successful authentication (register / login / refresh).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String accessToken;
    private String refreshToken;

    @Builder.Default
    private String tokenType = "Bearer";

    private Long userId;
    private String email;
    private String name;
    private String currency;
    private Boolean darkMode;
}
