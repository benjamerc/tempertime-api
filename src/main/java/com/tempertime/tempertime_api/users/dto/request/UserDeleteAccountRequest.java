package com.tempertime.tempertime_api.users.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User account deletion request.
 */
@Schema(
        description = "Request for permanently deleting the authenticated user's account"
)
public record UserDeleteAccountRequest(

        @Schema(
                description = "Current account password",
                example = "Password123"
        )
        @NotBlank(message = "Current password is required")
        @Size(max = 255, message = "Password must be at most 255 characters")
        String currentPassword
) {}
