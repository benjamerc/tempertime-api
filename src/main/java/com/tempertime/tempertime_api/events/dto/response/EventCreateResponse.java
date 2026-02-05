package com.tempertime.tempertime_api.events.dto.response;

import java.time.Instant;

/** Response DTO after creating a new Event */
public record EventCreateResponse(

        Long id,
        String title,
        Instant eventDate,
        Instant createdAt
) {}
