package com.tempertime.tempertime_api.events.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tempertime.tempertime_api.events.deserializer.EventOffsetDateTimeDeserializer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * Represents a partial update payload for an Event.
 */
@Schema(
        description = "Event partial update request"
)
public record EventUpdateRequest(

        @Schema(
                description = "Event title",
                example = "Project launch",
                nullable = true
        )
        @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
        String title,

        @Schema(
                description = "Event date and time in ISO 8601 format with mandatory UTC offset",
                example = "2026-02-22T07:30-03:00",
                nullable = true
        )
        // Strict ISO-8601 date-time with explicit offset (yyyy-MM-dd'T'HH:mm±HH:mm)
        @JsonDeserialize(using = EventOffsetDateTimeDeserializer.class)
        @FutureOrPresent(message = "Event date must be in the present or future")
        OffsetDateTime eventDate,

        @Schema(
                description = "Event description",
                example = "Project kickoff meeting",
                nullable = true
        )
        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        @Schema(
                description = "Event hex color code",
                example = "#2ECC71",
                nullable = true
        )
        @Size(min = 4, max = 7, message = "Color must be between 4 and 7 characters")
        String color
) {}
