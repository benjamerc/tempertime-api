package com.tempertime.tempertime_api.events.service.period;

import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.exception.InvalidEventPeriodException;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Converts a string from a request parameter to an EventPeriod enum.
 * Accepts any case (upper/lower) and throws InvalidEventPeriodException if invalid.
 */
@Component
public class EventPeriodConverter implements Converter<String, EventPeriod> {

    @Override
    public EventPeriod convert(String source) {
        try {
            return EventPeriod.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidEventPeriodException("Invalid event period");
        }
    }
}
