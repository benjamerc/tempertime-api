package com.tempertime.tempertime_api.users.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Authenticated user profile response.
 */
@Schema(
        description = "Authenticated user profile"
)
public record UserProfileResponse(

        @Schema(
                description = "User unique identifier",
                example = "1"
        )
        Long id,

        @Schema(
                description = "User email address",
                example = "john.doe@example.com"
        )
        String email,

        @Schema(
                description = "User first name",
                example = "John"
        )
        String firstName,

        @Schema(
                description = "User last name",
                example = "Doe"
        )
        String lastName,

        @Schema(
                description = "Account creation timestamp in ISO 8601 format (UTC)",
                example = "2026-02-07T08:15:30.339652Z"
        )
        Instant createdAt
) {}
