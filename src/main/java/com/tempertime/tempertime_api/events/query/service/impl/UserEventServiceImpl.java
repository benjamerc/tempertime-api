package com.tempertime.tempertime_api.events.query.service.impl;

import com.tempertime.tempertime_api.events.model.Event;
import com.tempertime.tempertime_api.events.query.dto.internal.TimeRange;
import com.tempertime.tempertime_api.events.query.dto.response.UserEventResponse;
import com.tempertime.tempertime_api.events.query.exception.InvalidEventPeriodException;
import com.tempertime.tempertime_api.events.query.mapper.UserEventMapper;
import com.tempertime.tempertime_api.events.query.model.EventPeriod;
import com.tempertime.tempertime_api.events.query.service.UserEventService;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserEventServiceImpl implements UserEventService {

    private final EventRepository eventRepository;
    private final UserEventMapper userEventMapper;

    @Override
    public List<UserEventResponse> getUserEvents(
            Long userId,
            EventPeriod period,
            ZoneId timeZone
    ) {

        List<Event> events;

        if (period == EventPeriod.ALL) {
            events = eventRepository.findAllByUserId(userId);
        } else {
            TimeRange range = resolvePeriodRange(period, timeZone);
            events = eventRepository.findAllByUserIdAndDateRange(
                    userId,
                    range.start(),
                    range.end()
            );
        }

        return events.stream()
                .map(userEventMapper::toUserEventResponse)
                .toList();
    }

    /** Calculates the start and end Instants for the given EventPeriod */
    private TimeRange resolvePeriodRange(EventPeriod period, ZoneId zone) {

        ZonedDateTime now = ZonedDateTime.now(zone);
        ZonedDateTime start = now.truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime end;

        switch (period) {
            case DAY -> end = start.plusDays(1);
            case WEEK -> end = start.plusWeeks(1);
            case MONTH -> end = start.plusMonths(1);
            default -> throw new InvalidEventPeriodException("Invalid event period");
        }

        return new TimeRange(
                start.toInstant(),
                end.toInstant()
        );
    }
}
