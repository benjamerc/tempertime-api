package com.tempertime.tempertime_api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request containing a refresh token.
 */
@Schema(
        description = "Request containing a refresh token used for token rotation"
)
public record AuthRefreshTokenRequest(

        @Schema(
                description = "Refresh token value",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        @NotNull(message = "Refresh token is required")
        UUID refreshToken
) {}
