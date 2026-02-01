package com.tempertime.tempertime_api.users.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** User account deletion request */
public record UserDeleteAccountRequest(

        @NotBlank(message = "Current password is required")
        @Size(max = 255, message = "Password must be at most 255 characters")
        String currentPassword
) {}
