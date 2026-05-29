package com.tempertime.tempertime_api.users.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

/**
 * User profile partial update request.
 */
@Schema(
        description = "Request for partially updating the authenticated user's profile"
)
public record UserUpdateProfileRequest(

        @Schema(
                description = "User first name",
                example = "John",
                nullable = true
        )
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        String firstName,

        @Schema(
                description = "User last name",
                example = "Doe",
                nullable = true
        )
        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        String lastName
) {}
