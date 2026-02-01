package com.tempertime.tempertime_api.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** User password update request */
public record UserUpdatePasswordRequest(

        @NotBlank(message = "Current password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        String newPassword
) {}
