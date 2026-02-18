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

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserEventServiceImpl implements UserEventService {

    private final EventRepository eventRepository;
    private final UserEventMapper userEventMapper;
    private final EventPeriodResolver eventPeriodResolver;

    /**
     * Retrieves events assigned to a user within the specified period.
     *
     * Behavior:
     * - DAY/WEEK/MONTH: requires a non-null timeZone to calculate the time range.
     * - ALL: ignores timeZone and returns all events without date filtering.
     *
     * Throws InvalidEventPeriodException if timeZone is missing for DAY/WEEK/MONTH.
     */
    @Transactional(readOnly = true)
    @Override
    public List<UserEventResponse> getUserEvents(
            Long userId,
            EventPeriod period,
            ZoneId timeZone
    ) {

        // Validates that timeZone is provided for periods that require it
        if (period != EventPeriod.ALL && timeZone == null) {
            throw new TimeZoneMissingException();
        }

        Optional<TimeRange> range =
                (period == EventPeriod.ALL)
                        ? Optional.empty()
                        : eventPeriodResolver.resolve(period, timeZone);

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
