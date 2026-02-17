package com.tempertime.tempertime_api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

/**
 * Request containing a refresh token.
 */
public record AuthRefreshTokenRequest(

        @NotBlank(message = "Refresh token is required")
        UUID refreshToken
) {}
