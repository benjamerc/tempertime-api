package com.tempertime.tempertime_api.events.dto.internal;

import java.time.Instant;

/**
 * Represents a start and end Instant for filtering events.
 */
public record TimeRange(

        Instant start,
        Instant end
) {}
