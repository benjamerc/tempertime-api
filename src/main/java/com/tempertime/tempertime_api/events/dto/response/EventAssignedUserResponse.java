package com.tempertime.tempertime_api.events.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response containing the users assigned to an event.
 */
@Schema(
        description = "Users assigned to an event"
)
public record EventAssignedUserResponse(

        @Schema(
                description = "User ID",
                example = "1"
        )
        Long userId,

        @Schema(
                description = "User first name",
                example = "John"
        )
        String firstName,

        @Schema(
                description = "User last name",
                example = "Doe"
        )
        String lastName
) {}
