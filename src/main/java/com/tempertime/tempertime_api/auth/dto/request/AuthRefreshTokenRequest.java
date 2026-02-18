package com.tempertime.tempertime_api.auth.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request containing a refresh token.
 */
public record AuthRefreshTokenRequest(

        @NotNull(message = "Refresh token is required")
        UUID refreshToken
) {}
