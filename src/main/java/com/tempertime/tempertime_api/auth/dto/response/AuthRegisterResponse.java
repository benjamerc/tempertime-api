package com.tempertime.tempertime_api.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response returned after successful user registration.
 */
@Schema(
        description = "Response returned after successful user registration"
)
public record AuthRegisterResponse(

        @Schema(
                description = "Registered user email",
                example = "john.doe@example.com"
        )
        String email
) {}
