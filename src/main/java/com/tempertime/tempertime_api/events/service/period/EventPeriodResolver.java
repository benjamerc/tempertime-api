package com.tempertime.tempertime_api.events.service.period;

import com.tempertime.tempertime_api.events.dto.internal.TimeRange;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.exception.InvalidEventPeriodException;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Optional;

/**
 * Resolves the natural time boundaries for a given EventPeriod
 * using the provided time zone.
 *
 * The returned TimeRange represents the start of the period
 * and the start of the next period.
 *
 * Examples:
 * - DAY   -> [start of current day, start of next day)
 * - WEEK  -> [start of current week, start of next week)
 * - MONTH -> [start of current month, start of next month)
 *
 * For ALL, no filtering is applied and an empty Optional is returned.
 *
 * Note: EventRepository applies the range using ">= start" and "< end".
 */
@Component
public class EventPeriodResolver {

    public Optional<TimeRange> resolve(
            EventPeriod period,
            ZoneId zone,
            ZonedDateTime baseDate) {

        if (period == EventPeriod.ALL) {
            return Optional.empty();
        }

        ZonedDateTime now = (baseDate != null)
                ? baseDate
                : ZonedDateTime.now(zone);

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
                end = start.plusWeeks(1);
            }
            case MONTH -> {
                start = now.with(TemporalAdjusters.firstDayOfMonth())
                        .truncatedTo(ChronoUnit.DAYS);
                end = start.plusMonths(1);
            }
            default -> throw new InvalidEventPeriodException();
        }

        return Optional.of(
                new TimeRange(start.toInstant(), end.toInstant())
        );
    }
}
