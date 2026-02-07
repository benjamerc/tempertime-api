package com.tempertime.tempertime_api.events.dto.response;

import java.time.Instant;

/**
 * Response DTO for GET by ID and PATCH.
 * Returns all event fields.
 */
public record EventResponse(

        Long id,
        String title,
        Instant eventDate,
        Instant createdAt,
        String description,
        String scope,
        String color,
        Boolean hasActiveUsers
) {}
