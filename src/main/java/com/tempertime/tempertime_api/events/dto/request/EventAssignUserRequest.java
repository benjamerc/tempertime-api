package com.tempertime.tempertime_api.events.dto.request;

import com.tempertime.tempertime_api.events.validation.annotation.ExistingUserIds;
import com.tempertime.tempertime_api.events.validation.annotation.NoDuplicateUserIds;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for assigning users to an event.
 */
@Schema(
        description = "Event user assignment request"
)
public record EventAssignUserRequest(

        @Schema(
                description = "User IDs to assign to the event",
                example = "[1, 2, 3]"
        )
        @NotNull(message = "User IDs list is required")
        @NotEmpty(message = "At least one user must be assigned to the event")
        @NoDuplicateUserIds
        @ExistingUserIds
        List<Long> userIds
) {}
