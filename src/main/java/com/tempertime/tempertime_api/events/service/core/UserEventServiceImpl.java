package com.tempertime.tempertime_api.events.service.core;

import com.tempertime.tempertime_api.events.exception.TimeZoneMissingException;
import com.tempertime.tempertime_api.events.service.period.EventPeriodResolver;
import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.dto.internal.TimeRange;
import com.tempertime.tempertime_api.events.dto.response.UserEventResponse;
import com.tempertime.tempertime_api.events.mapper.UserEventMapper;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserEventServiceImpl implements UserEventService {

    private final EventRepository eventRepository;
    private final UserEventMapper userEventMapper;
    private final EventPeriodResolver eventPeriodResolver;

    /**
     * <p>Retrieves events assigned to a user within the specified period.</p>
     *
     * <p><strong>Behavior:</strong></p>
     * <ul>
     *   <li><code>DAY</code>/<code>WEEK</code>/<code>MONTH</code>: requires a non-null <code>timeZone</code> to calculate the time range.
     *     If a date is provided, the period is calculated relative to that date.
     *     Otherwise, the current date-time is used.</li>
     *   <li><code>ALL</code>: ignores <code>timeZone</code> and returns all events without date filtering.</li>
     * </ul>
     *
     * <p>Throws <code>TimeZoneMissingException</code> if <code>timeZone</code> is missing for
     * <code>DAY</code>/<code>WEEK</code>/<code>MONTH</code>.</p>
     */
    @Transactional(readOnly = true)
    @Override
    public List<UserEventResponse> getUserEvents(
            Long userId,
            EventPeriod period,
            ZoneId timeZone,
            LocalDate date
    ) {

        // Validates that timeZone is provided for periods that require it
        if (period != EventPeriod.ALL && timeZone == null) {
            throw new TimeZoneMissingException();
        }

        ZonedDateTime baseDate = (date != null)
                ? date.atStartOfDay(timeZone)
                : null;

        Optional<TimeRange> range =
                (period == EventPeriod.ALL)
                        ? Optional.empty()
                        : eventPeriodResolver.resolve(period, timeZone, baseDate);

        List<Event> events = range
                .map(r -> eventRepository
                        .findAllByUserIdAndDateRange(
                                userId,
                                r.start(),
                                r.end()
                        ))
                .orElseGet(() ->
                        eventRepository.findAllByUserId(userId)
                );

        return events.stream()
                .map(userEventMapper::toUserEventResponse)
                .toList();
    }
}
