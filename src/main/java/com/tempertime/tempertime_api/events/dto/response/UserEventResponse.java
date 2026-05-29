package com.tempertime.tempertime_api.events.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response DTO representing events assigned to the authenticated user.
 */
@Schema(
        description = "Event assigned to the authenticated user"
)
public record UserEventResponse(

        @Schema(
                description = "Event unique identifier",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Event title",
                example = "Project launch"
        )
        String title,

        @Schema(
                description = "Event date and time in ISO 8601 format (UTC)",
                example = "2026-02-20T10:30:00Z"
        )
        Instant eventDate,

        @Schema(
                description = "Event hex color code",
                example = "#3498DB"
        )
        String color,

        @Schema(
                description = "Workspace unique identifier",
                example = "1"
        )
        Long workspaceId,

        @Schema(
                description = "Workspace name",
                example = "Alpha Project"
        )
        String workspaceName,

        @Schema(
                description = "Workspace hex color code",
                example = "#E67E22"
        )
        String workspaceColor
) {}
