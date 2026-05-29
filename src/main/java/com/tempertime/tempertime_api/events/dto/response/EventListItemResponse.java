package com.tempertime.tempertime_api.events.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Lightweight event view used in user events listings.
 */
@Schema(
        description = "Event information used in workspace event listings"
)
public record EventListItemResponse(

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
                description = "Event date and time in ISO 8601 (UTC)",
                example = "2026-02-20T10:30:00Z"
        )
        Instant eventDate,

        @Schema(
                description = "Event hex color code",
                example = "#3498DB"
        )
        String color,

        @Schema(
                description = "Whether the event has assigned users besides the workspace owner",
                example = "true"
        )
        Boolean hasActiveUsers
) {}
