package com.tempertime.tempertime_api.events.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tempertime.tempertime_api.events.deserializer.EventOffsetDateTimeDeserializer;
import com.tempertime.tempertime_api.events.domain.EventScope;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/**
 * Request DTO for creating a new Event.
 */
@Schema(
        description = "Request containing the information required to create a new event"
)
public record EventCreateRequest(

        @Schema(
                description = "Event title",
                example = "Project launch"
        )
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
        String title,

        @Schema(
                description = "Event date and time in ISO 8601 format with mandatory UTC offset",
                example = "2026-02-20T07:30-03:00"
        )
        // Strict ISO-8601 date-time with explicit offset (yyyy-MM-dd'T'HH:mm±HH:mm)
        @JsonDeserialize(using = EventOffsetDateTimeDeserializer.class)
        @NotNull(message = "Event date is required")
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
                description = "Event assignment scope (GLOBAL or SPECIFIC)",
                example = "GLOBAL"
        )
        @NotNull(message = "Scope is required")
        EventScope scope,

        @Schema(
                description = "Event hex color code. If not specified, one is assigned automatically",
                example = "#3498DB",
                nullable = true
        )
        @Size(min = 4, max = 7, message = "Color must be between 4 and 7 characters")
        String color
) {}
