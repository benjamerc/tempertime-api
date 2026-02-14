package com.tempertime.tempertime_api.users.dto.request;

import jakarta.validation.constraints.Size;

/**
 * User profile partial update request.
 */
public record UserUpdateProfileRequest(

        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        String firstName,

        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        String lastName
) {}
