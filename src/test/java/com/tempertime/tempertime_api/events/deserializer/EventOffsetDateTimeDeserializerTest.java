package com.tempertime.tempertime_api.events.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.tempertime.tempertime_api.events.exception.InvalidEventDateFormatException;
import com.tempertime.tempertime_api.events.exception.InvalidEventDateValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EventOffsetDateTimeDeserializerTest {

    private final EventOffsetDateTimeDeserializer deserializer =
            new EventOffsetDateTimeDeserializer();

    @Mock
    private JsonParser jsonParser;

    @Mock
    private DeserializationContext deserializationContext;

    private OffsetDateTime deserialize(String value) throws IOException {
        when(jsonParser.getText()).thenReturn(value);
        return deserializer.deserialize(jsonParser, deserializationContext);
    }

    @Test
    void shouldDeserialize_whenValidFormat() throws IOException {

        OffsetDateTime result = deserialize("2026-05-02T10:30+03:00");

        assertThat(result).isNotNull();
        assertThat(result.toInstant())
                .isEqualTo(OffsetDateTime.parse("2026-05-02T10:30+03:00",
                        DateTimeFormatter.ISO_OFFSET_DATE_TIME).toInstant());
    }

    @Test
    void shouldThrowInvalidEventDateFormatException_whenSecondsAreIncluded() {

        assertThatThrownBy(() -> deserialize("2026-05-02T10:30:00+03:00"))
                .isInstanceOf(InvalidEventDateFormatException.class);
    }

    @Test
    void shouldThrowInvalidEventDateFormatException_whenZIsUsedInsteadOfOffset() {

        assertThatThrownBy(() -> deserialize("2026-05-02T10:30Z"))
                .isInstanceOf(InvalidEventDateFormatException.class);
    }

    @Test
    void shouldThrowInvalidEventDateFormatException_whenOffsetIsMissing() {

        assertThatThrownBy(() -> deserialize("2026-05-02T10:30"))
                .isInstanceOf(InvalidEventDateFormatException.class);
    }

    @Test
    void shouldThrowInvalidEventDateFormatException_whenFormatIsInvalid() {

        assertThatThrownBy(() -> deserialize("not-a-date"))
                .isInstanceOf(InvalidEventDateFormatException.class);
    }

    @Test
    void shouldThrowInvalidEventDateValueException_whenMonthIsInvalid() {

        assertThatThrownBy(() -> deserialize("2026-13-02T10:30+03:00"))
                .isInstanceOf(InvalidEventDateValueException.class);
    }

    @Test
    void shouldThrowInvalidEventDateValueException_whenDayIsInvalid() {

        assertThatThrownBy(() -> deserialize("2026-05-32T10:30+03:00"))
                .isInstanceOf(InvalidEventDateValueException.class);
    }
}