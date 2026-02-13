package com.tempertime.tempertime_api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request containing a refresh token.
 */
public record AuthRefreshTokenRequest(

        @NotBlank(message = "Refresh token is required")
        @Size(max = 64, message = "Refresh token must be at most 64 characters")
        String refreshToken
) {}
