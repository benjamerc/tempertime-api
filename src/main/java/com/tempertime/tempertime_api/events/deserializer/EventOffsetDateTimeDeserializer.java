package com.tempertime.tempertime_api.events.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.tempertime.tempertime_api.events.exception.InvalidEventDateFormatException;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;

/**
 * Jackson deserializer for Event date.
 * Enforces ISO-8601 format with explicit offset: YYYY-MM-DDTHH:mm±HH:MM.
 */
public class EventOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    /**
     * Strict formatter that accepts only ISO-8601 date-time with offset (±HH:MM).
     * Seconds and 'Z' (UTC designator) are not allowed.
     */
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mmXXX", Locale.ROOT)
                    .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext deserializationContext)
            throws IOException {

        try {
            return OffsetDateTime.parse(p.getText(), FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new InvalidEventDateFormatException(
                    "Invalid event date format", ex
            );
        }
    }
}
