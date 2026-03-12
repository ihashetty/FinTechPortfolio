package com.niveshtrack.portfolio.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for {@code PUT /api/user/profile}.
 * All fields are optional — only non-null fields will be updated.
 */
@Data
public class UpdateProfileRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;

    @Size(max = 10, message = "Currency code must not exceed 10 characters")
    private String currency;

    private Boolean darkMode;
}
