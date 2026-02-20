package com.tempertime.tempertime_api.events.service.rules;

import com.tempertime.tempertime_api.events.config.EventConstraintsProperties;
import com.tempertime.tempertime_api.events.exception.EventDateLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

/**
 * Validates event dates according to business constraints.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventDateRules {

    private final EventConstraintsProperties properties;

    /**
     * Checks that the event date is within the allowed range.
     */
    public void validateDateRange(OffsetDateTime eventDate) {

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime maxAllowed = now.plusMonths(properties.getMaxMonthsAhead());

        if (eventDate.isAfter(maxAllowed)) {
            throw new EventDateLimitExceededException();
        }
    }
}
