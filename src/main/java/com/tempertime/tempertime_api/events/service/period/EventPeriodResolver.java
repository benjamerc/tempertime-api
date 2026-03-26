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
 * <p>Resolves the natural time boundaries for a given <code>EventPeriod</code>
 * using the provided time zone and an optional base date.</p>
 *
 * <p>The returned <code>TimeRange</code> represents the start of the period
 * and the start of the next period.</p>
 *
 * <p>If a base date is provided, the period is calculated relative to that date.
 * Otherwise, the current date-time is used.</p>
 *
 * <p>Examples:</p>
 * <ul>
 *   <li><code>DAY</code>   -&gt; [start of the day, start of next day)</li>
 *   <li><code>WEEK</code>  -&gt; [start of the week, start of next week)</li>
 *   <li><code>MONTH</code> -&gt; [start of the month, start of next month)</li>
 * </ul>
 *
 * <p>For <code>ALL</code>, no filtering is applied and an empty <code>Optional</code> is returned.</p>
 *
 * <p><strong>Note:</strong> <code>EventRepository</code> applies the range using
 * <code>&gt;= start</code> and <code>&lt; end</code>.</p>
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
