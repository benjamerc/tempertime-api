package com.tempertime.tempertime_api.events.filter;

import com.tempertime.tempertime_api.events.dto.internal.TimeRange;
import com.tempertime.tempertime_api.events.model.EventPeriod;
import com.tempertime.tempertime_api.events.query.exception.InvalidEventPeriodException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

/**
 * Resolves filtering for an EventPeriod.
 *
 * For DAY, WEEK, or MONTH, returns the corresponding TimeRange:
 * - DAY: from midnight at the start of the current day to midnight of the next day.
 * - WEEK: from 00:00 on Monday of the current week to 23:59:59.999 on Sunday of the same week.
 * - MONTH: from 00:00 on the first day of the current month to 23:59:59.999 on the last day of the month.
 *
 * For ALL, no filtering is applied and an empty Optional is returned.
 */
@Component
public class EventPeriodResolver {

    public Optional<TimeRange> resolve(EventPeriod period, ZoneId zone) {

        if (period == EventPeriod.ALL) {
            return Optional.empty(); // Entirety, without restriction
        }

        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime start;
        ZonedDateTime end;

        switch (period) {
            case DAY -> {
                start = now.truncatedTo(ChronoUnit.DAYS);
                end = start.plusDays(1);
            }
            case WEEK -> {
                start = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .truncatedTo(ChronoUnit.DAYS);
                end = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                        .with(LocalTime.MAX);
            }
            case MONTH -> {
                start = now.with(TemporalAdjusters.firstDayOfMonth())
                        .truncatedTo(ChronoUnit.DAYS);
                end = now.with(TemporalAdjusters.lastDayOfMonth())
                        .with(LocalTime.MAX);
            }
            default -> throw new InvalidEventPeriodException("Invalid event period");
        }

        return Optional.of(
                new TimeRange(start.toInstant(), end.toInstant())
        );
    }
}
