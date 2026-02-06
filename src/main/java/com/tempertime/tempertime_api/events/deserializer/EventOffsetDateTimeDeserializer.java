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
import java.util.regex.Pattern;

/**
 * Custom deserializer for event date-time.
 * Accepts only ISO-8601 with explicit offset: yyyy-MM-dd'T'HH:mm±HH:mm.
 * Seconds and 'Z' are intentionally not allowed.
 */
public class EventOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    // Enforces exact structure: yyyy-MM-dd'T'HH:mm±HH:mm
    private static final Pattern FORMAT_PATTERN =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}[+-]\\d{2}:\\d{2}$");

    // Strict parsing to reject invalid calendar dates
    private static final DateTimeFormatter ISO_STRICT =
            DateTimeFormatter.ISO_OFFSET_DATE_TIME
                    .withResolverStyle(ResolverStyle.STRICT);

    @Override
    public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {

        String value = jsonParser.getText();

        // Fast structural validation
        if (!FORMAT_PATTERN.matcher(value).matches()) {
            throw new InvalidEventDateFormatException(
                    "Invalid event date format"
            );
        }

        try {
            // Semantic validation
            return OffsetDateTime.parse(value, ISO_STRICT);
        } catch (DateTimeParseException ex) {
            throw new InvalidEventDateFormatException(
                    "Invalid event date value", ex
            );
        }
    }
}
