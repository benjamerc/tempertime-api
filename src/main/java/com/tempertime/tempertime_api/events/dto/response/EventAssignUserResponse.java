package com.tempertime.tempertime_api.events.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Response returned after assigning users to an event.
 */
@Schema(
        description = "Event user assignment response"
)
public record EventAssignUserResponse(

        @Schema(
                description = "Event ID",
                example = "1"
        )
        Long eventId,

        @Schema(
                description = "Assigned user IDs",
                example = "[1, 2, 3]"
        )
        List<Long> userIds
) {}
