package com.tempertime.tempertime_api.events.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response DTO for Event retrieval and partial update.
 * Contains all event fields.
 */
@Schema(
        description = "Detailed event information"
)
public record EventResponse(

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
                description = "Event creation timestamp in ISO 8601 (UTC)",
                example = "2026-02-07T13:15:30.339652Z"
        )
        Instant createdAt,

        @Schema(
                description = "Event description",
                example = "Project kickoff meeting",
                nullable = true
        )
        String description,

        @Schema(
                description = "Event assignment scope (GLOBAL or SPECIFIC)",
                example = "GLOBAL"
        )
        String scope,

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
