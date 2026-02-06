package com.tempertime.tempertime_api.events.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tempertime.tempertime_api.events.deserializer.EventOffsetDateTimeDeserializer;
import com.tempertime.tempertime_api.events.model.EventScope;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/** Request DTO for creating a new Event */
public record EventCreateRequest(

        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
        String title,

        // Strict ISO-8601 date-time with explicit offset (yyyy-MM-dd'T'HH:mm±HH:mm)
        @JsonDeserialize(using = EventOffsetDateTimeDeserializer.class)
        @NotNull(message = "Event date is required")
        @FutureOrPresent(message = "Event date must be in the present or future")
        OffsetDateTime eventDate,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        @NotNull(message = "Scope is required")
        EventScope scope,

        @Size(min = 4, max = 7, message = "Color must be between 4 and 7 characters")
        String color
) {}
