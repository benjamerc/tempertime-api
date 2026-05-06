package com.tempertime.tempertime_api.events.service.period;

import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.exception.InvalidEventPeriodException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EventPeriodConverterTest {

    private final EventPeriodConverter converter = new EventPeriodConverter();

    @Test
    void shouldConvertToEventPeriod_whenValidValueProvided() {
        assertThat(converter.convert("DAY")).isEqualTo(EventPeriod.DAY);
        assertThat(converter.convert("WEEK")).isEqualTo(EventPeriod.WEEK);
        assertThat(converter.convert("MONTH")).isEqualTo(EventPeriod.MONTH);
        assertThat(converter.convert("ALL")).isEqualTo(EventPeriod.ALL);
    }

    @Test
    void shouldConvertToEventPeriod_whenLowercaseValueProvided() {
        assertThat(converter.convert("day")).isEqualTo(EventPeriod.DAY);
        assertThat(converter.convert("week")).isEqualTo(EventPeriod.WEEK);
        assertThat(converter.convert("month")).isEqualTo(EventPeriod.MONTH);
        assertThat(converter.convert("all")).isEqualTo(EventPeriod.ALL);
    }

    @Test
    void shouldConvertToEventPeriod_whenMixedCaseValueProvided() {
        assertThat(converter.convert("Day")).isEqualTo(EventPeriod.DAY);
        assertThat(converter.convert("Week")).isEqualTo(EventPeriod.WEEK);
    }

    @Test
    void shouldThrowInvalidEventPeriodException_whenInvalidValueProvided() {
        assertThatThrownBy(() -> converter.convert("INVALID"))
                .isInstanceOf(InvalidEventPeriodException.class);
    }
}
