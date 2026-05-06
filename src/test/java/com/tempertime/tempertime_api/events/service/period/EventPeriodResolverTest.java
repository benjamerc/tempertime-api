package com.tempertime.tempertime_api.events.service.period;

import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.dto.internal.TimeRange;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class EventPeriodResolverTest {

    private final EventPeriodResolver resolver = new EventPeriodResolver();

    private static final ZoneId ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    @Test
    void shouldReturnEmpty_whenPeriodIsAll() {

        Optional<TimeRange> result = resolver.resolve(EventPeriod.ALL, ZONE, null);

        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnDayRange_whenPeriodIsDay() {

        ZonedDateTime baseDate = ZonedDateTime.of(2026, 5, 2, 15, 30, 0, 0, ZONE);

        Optional<TimeRange> result = resolver.resolve(EventPeriod.DAY, ZONE, baseDate);

        assertThat(result).isPresent();

        Instant expectedStart = ZonedDateTime.of(2026, 5, 2, 0, 0, 0, 0, ZONE).toInstant();
        Instant expectedEnd = ZonedDateTime.of(2026, 5, 3, 0, 0, 0, 0, ZONE).toInstant();

        assertThat(result.get().start()).isEqualTo(expectedStart);
        assertThat(result.get().end()).isEqualTo(expectedEnd);
    }

    @Test
    void shouldReturnWeekRange_whenPeriodIsWeek() {

        // 2026-05-02 is a Saturday - week should start on Monday 2026-04-27
        ZonedDateTime baseDate = ZonedDateTime.of(2026, 5, 2, 15, 30, 0, 0, ZONE);

        Optional<TimeRange> result = resolver.resolve(EventPeriod.WEEK, ZONE, baseDate);

        assertThat(result).isPresent();

        Instant expectedStart = ZonedDateTime.of(2026, 4, 27, 0, 0, 0, 0, ZONE).toInstant();
        Instant expectedEnd = ZonedDateTime.of(2026, 5, 4, 0, 0, 0, 0, ZONE).toInstant();

        assertThat(result.get().start()).isEqualTo(expectedStart);
        assertThat(result.get().end()).isEqualTo(expectedEnd);
    }

    @Test
    void shouldReturnMonthRange_whenPeriodIsMonth() {

        ZonedDateTime baseDate = ZonedDateTime.of(2026, 5, 15, 15, 30, 0, 0, ZONE);

        Optional<TimeRange> result = resolver.resolve(EventPeriod.MONTH, ZONE, baseDate);

        assertThat(result).isPresent();

        Instant expectedStart = ZonedDateTime.of(2026, 5, 1, 0, 0, 0, 0, ZONE).toInstant();
        Instant expectedEnd = ZonedDateTime.of(2026, 6, 1, 0, 0, 0, 0, ZONE).toInstant();

        assertThat(result.get().start()).isEqualTo(expectedStart);
        assertThat(result.get().end()).isEqualTo(expectedEnd);
    }

    @Test
    void shouldUseCurrentDate_whenBaseDateIsNull() {

        Optional<TimeRange> result = resolver.resolve(EventPeriod.DAY, ZONE, null);

        assertThat(result).isPresent();
        assertThat(result.get().start()).isBefore(result.get().end());
    }

    @Test
    void shouldReturnDayRange_whenBaseDateIsStartOfDay() {

        ZonedDateTime baseDate = ZonedDateTime.of(2026, 5, 2, 0, 0, 0, 0, ZONE);

        Optional<TimeRange> result = resolver.resolve(EventPeriod.DAY, ZONE, baseDate);

        assertThat(result).isPresent();

        Instant expectedStart = ZonedDateTime.of(2026, 5, 2, 0, 0, 0, 0, ZONE).toInstant();
        Instant expectedEnd = ZonedDateTime.of(2026, 5, 3, 0, 0, 0, 0, ZONE).toInstant();

        assertThat(result.get().start()).isEqualTo(expectedStart);
        assertThat(result.get().end()).isEqualTo(expectedEnd);
    }

    @Test
    void shouldReturnWeekRange_whenBaseDateIsMonday() {

        // 2026-04-27 is a Monday - week should start on itself
        ZonedDateTime baseDate = ZonedDateTime.of(2026, 4, 27, 10, 0, 0, 0, ZONE);

        Optional<TimeRange> result = resolver.resolve(EventPeriod.WEEK, ZONE, baseDate);

        assertThat(result).isPresent();

        Instant expectedStart = ZonedDateTime.of(2026, 4, 27, 0, 0, 0, 0, ZONE).toInstant();
        Instant expectedEnd = ZonedDateTime.of(2026, 5, 4, 0, 0, 0, 0, ZONE).toInstant();

        assertThat(result.get().start()).isEqualTo(expectedStart);
        assertThat(result.get().end()).isEqualTo(expectedEnd);
    }
}
