package com.tempertime.tempertime_api.events.dto.response;

import java.time.Instant;

/** Lightweight event view used in user events listings */
public record EventListItemResponse(

        Long id,
        String title,
        Instant eventDate,
        String color,
        Boolean hasActiveUsers
) {}
