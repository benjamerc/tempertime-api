package com.tempertime.tempertime_api.events.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Response DTO after creating a new Event.
 */
@Schema(
        description = "Created event data"
)
public record EventCreateResponse(

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
        Instant createdAt
) {}
