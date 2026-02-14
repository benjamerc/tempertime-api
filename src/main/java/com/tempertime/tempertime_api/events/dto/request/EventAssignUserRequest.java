package com.tempertime.tempertime_api.events.dto.request;

import com.tempertime.tempertime_api.events.validation.annotation.ExistingUserIds;
import com.tempertime.tempertime_api.events.validation.annotation.NoDuplicateUserIds;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for assigning users to an event.
 */
public record EventAssignUserRequest(

        @NotNull(message = "User IDs list is required")
        @NotEmpty(message = "At least one user must be assigned to the event")
        @NoDuplicateUserIds
        @ExistingUserIds
        List<Long> userIds
) {}
