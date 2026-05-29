package com.tempertime.tempertime_api.users.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User password update request.
 */
@Schema(
        description = "Request for updating the authenticated user's password"
)
public record UserUpdatePasswordRequest(

        @Schema(
                description = "Current account password",
                example = "Password123"
        )
        @NotBlank(message = "Current password is required")
        @Size(max = 255, message = "Password must be at most 255 characters")
        String currentPassword,

        @Schema(
                description = "New account password",
                example = "NewPassword456"
        )
        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        String newPassword
) {}
