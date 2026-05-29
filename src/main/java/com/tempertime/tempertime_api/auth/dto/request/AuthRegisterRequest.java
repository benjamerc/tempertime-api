package com.tempertime.tempertime_api.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * User registration request.
 */
@Schema(
        description = "Request containing the information required to register a new user account"
)
public record AuthRegisterRequest(

        @Schema(
                description = "User email address",
                example = "john.doe@example.com"
        )
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,

        @Schema(
                description = "User first name",
                example = "John"
        )
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        String firstName,

        @Schema(
                description = "User last name",
                example = "Doe"
        )
        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        String lastName,

        @Schema(
                description = "User account password",
                example = "Password123"
        )
        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255 characters")
        String password
) {}
