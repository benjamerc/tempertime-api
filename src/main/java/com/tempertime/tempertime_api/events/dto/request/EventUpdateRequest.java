package com.tempertime.tempertime_api.events.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.tempertime.tempertime_api.events.deserializer.EventOffsetDateTimeDeserializer;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

/** Event partial update request */
public record EventUpdateRequest(

        @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
        String title,

        // Strict ISO-8601 date-time with explicit offset (yyyy-MM-dd'T'HH:mm±HH:mm)
        @JsonDeserialize(using = EventOffsetDateTimeDeserializer.class)
        @FutureOrPresent(message = "Event date must be in the present or future")
        OffsetDateTime eventDate,

        @Size(max = 500, message = "Description must be at most 500 characters")
        String description,

        @Size(min = 4, max = 7, message = "Color must be between 4 and 7 characters")
        String color
) {}
