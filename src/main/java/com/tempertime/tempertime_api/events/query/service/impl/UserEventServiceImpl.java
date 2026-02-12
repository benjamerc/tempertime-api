package com.tempertime.tempertime_api.events.query.service.impl;

import com.tempertime.tempertime_api.events.filter.EventPeriodResolver;
import com.tempertime.tempertime_api.events.model.Event;
import com.tempertime.tempertime_api.events.dto.internal.TimeRange;
import com.tempertime.tempertime_api.events.query.dto.response.UserEventResponse;
import com.tempertime.tempertime_api.events.query.mapper.UserEventMapper;
import com.tempertime.tempertime_api.events.model.EventPeriod;
import com.tempertime.tempertime_api.events.query.service.UserEventService;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
     * Retrieves the user's events for the given period and time zone.
     * Uses a time range filter for DAY, WEEK, MONTH; returns all events for ALL.
     */
    @Override
    public List<UserEventResponse> getUserEvents(
            Long userId,
            EventPeriod period,
            ZoneId timeZone
    ) {

        Optional<TimeRange> range =
                eventPeriodResolver.resolve(period, timeZone);

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
