package com.tempertime.tempertime_api.events.dto.response;

import java.time.Instant;

/**
 * Response DTO representing events assigned to the authenticated user.
 */
public record UserEventResponse(

        Long id,
        String title,
        Instant eventDate,
        String color,
        Long workspaceId,
        String workspaceName,
        String workspaceColor
) {}
