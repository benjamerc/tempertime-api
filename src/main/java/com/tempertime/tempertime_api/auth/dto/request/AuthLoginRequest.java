package com.tempertime.tempertime_api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Login request payload.
 */
@Schema(
        description = "User authentication request containing email and password credentials"
)
public record AuthLoginRequest(

        @Schema(
                description = "User email address",
                example = "john.doe@example.com"
        )
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,

        @Schema(
                description = "User account password",
                example = "Password123"
        )
        @NotBlank(message = "Password is required")
        @Size(max = 255, message = "Password must be at most 255 characters")
        String password
) {}
