package com.tempertime.tempertime_api.events.service.core;

import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.common.pagination.PaginationValidator;
import com.tempertime.tempertime_api.events.EventTestDataProvider;
import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.dto.internal.TimeRange;
import com.tempertime.tempertime_api.events.dto.response.*;
import com.tempertime.tempertime_api.events.exception.*;
import com.tempertime.tempertime_api.events.mapper.UserEventMapper;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.events.service.period.EventPeriodResolver;
import com.tempertime.tempertime_api.workspaces.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserEventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserEventMapper userEventMapper;

    @Mock
    private EventPeriodResolver eventPeriodResolver;

    @Mock
    private PaginationValidator paginationValidator;

    @InjectMocks
    private UserEventServiceImpl userEventService;

    @Nested
    class GetUserEventsTests {

        @Test
        void shouldReturnPagedEvents_whenPeriodIsAll() {

            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            Event event = EventTestDataProvider.event(1L, workspace);
            UserEventResponse userEventResponse = EventTestDataProvider.userEventResponse(event);
            Page<Event> page = new PageImpl<>(List.of(event));

            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventRepository.findAllByUserId(userId, pageable)).thenReturn(page);
            when(userEventMapper.toUserEventResponse(event)).thenReturn(userEventResponse);

            PageResponse<UserEventResponse> result =
                    userEventService.getUserEvents(userId, EventPeriod.ALL, null, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0)).usingRecursiveComparison().isEqualTo(userEventResponse);

            verify(paginationValidator).validate(pageable);
            verify(eventRepository).findAllByUserId(userId, pageable);
            verify(eventPeriodResolver, never()).resolve(any(), any(), any());
            verify(eventRepository, never()).findAllByUserIdAndDateRange(any(), any(), any(), any());
        }

        @Test
        void shouldReturnPagedEvents_whenPeriodIsDayAndTimeZoneProvided() {

            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();
            ZoneId zone = ZoneId.of("America/Argentina/Buenos_Aires");

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            Event event = EventTestDataProvider.event(1L, workspace);
            UserEventResponse userEventResponse = EventTestDataProvider.userEventResponse(event);

            Instant start = Instant.now().minusSeconds(3600);
            Instant end = Instant.now().plusSeconds(3600);
            TimeRange range = new TimeRange(start, end);
            Page<Event> page = new PageImpl<>(List.of(event));

            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventPeriodResolver.resolve(eq(EventPeriod.DAY), eq(zone), any()))
                    .thenReturn(Optional.of(range));
            when(eventRepository.findAllByUserIdAndDateRange(userId, start, end, pageable))
                    .thenReturn(page);
            when(userEventMapper.toUserEventResponse(event)).thenReturn(userEventResponse);

            PageResponse<UserEventResponse> result =
                    userEventService.getUserEvents(userId, EventPeriod.DAY, zone, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);

            verify(eventPeriodResolver).resolve(eq(EventPeriod.DAY), eq(zone), any());
            verify(eventRepository).findAllByUserIdAndDateRange(userId, start, end, pageable);
            verify(eventRepository, never()).findAllByUserId(any(), any());
        }

        @Test
        void shouldReturnPagedEvents_whenPeriodIsAllAndDateIsProvided() {

            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();
            LocalDate date = LocalDate.of(2026, 5, 2);

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            Event event = EventTestDataProvider.event(1L, workspace);
            UserEventResponse userEventResponse = EventTestDataProvider.userEventResponse(event);
            Page<Event> page = new PageImpl<>(List.of(event));

            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventRepository.findAllByUserId(userId, pageable)).thenReturn(page);
            when(userEventMapper.toUserEventResponse(event)).thenReturn(userEventResponse);

            PageResponse<UserEventResponse> result =
                    userEventService.getUserEvents(userId, EventPeriod.ALL, null, date, pageable);

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);

            verify(eventPeriodResolver, never()).resolve(any(), any(), any());
            verify(eventRepository).findAllByUserId(userId, pageable);
            verify(eventRepository, never()).findAllByUserIdAndDateRange(any(), any(), any(), any());
        }

        @Test
        void shouldReturnPagedEvents_whenPeriodIsWeekAndBaseDateProvided() {

            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();
            ZoneId zone = ZoneId.of("America/Argentina/Buenos_Aires");
            LocalDate date = LocalDate.of(2026, 5, 2);

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            Event event = EventTestDataProvider.event(1L, workspace);
            UserEventResponse userEventResponse = EventTestDataProvider.userEventResponse(event);

            Instant start = Instant.now().minusSeconds(3600);
            Instant end = Instant.now().plusSeconds(3600);
            TimeRange range = new TimeRange(start, end);
            Page<Event> page = new PageImpl<>(List.of(event));

            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventPeriodResolver.resolve(eq(EventPeriod.WEEK), eq(zone), any()))
                    .thenReturn(Optional.of(range));
            when(eventRepository.findAllByUserIdAndDateRange(userId, start, end, pageable))
                    .thenReturn(page);
            when(userEventMapper.toUserEventResponse(event)).thenReturn(userEventResponse);

            PageResponse<UserEventResponse> result =
                    userEventService.getUserEvents(userId, EventPeriod.WEEK, zone, date, pageable);

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);

            verify(eventPeriodResolver).resolve(eq(EventPeriod.WEEK), eq(zone), any());
            verify(eventRepository).findAllByUserIdAndDateRange(userId, start, end, pageable);
            verify(eventRepository, never()).findAllByUserId(any(), any());
        }

        @Test
        void shouldReturnEmptyPage_whenUserHasNoEvents() {

            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventRepository.findAllByUserId(userId, pageable)).thenReturn(Page.empty());

            PageResponse<UserEventResponse> result =
                    userEventService.getUserEvents(userId, EventPeriod.ALL, null, null, pageable);

            assertThat(result.content()).isEmpty();

            verify(userEventMapper, never()).toUserEventResponse(any());
        }

        @Test
        void shouldThrowTimeZoneMissingException_whenPeriodIsNotAllAndTimeZoneIsNull() {

            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            when(paginationValidator.validate(pageable)).thenReturn(pageable);

            assertThatThrownBy(() ->
                    userEventService.getUserEvents(userId, EventPeriod.DAY, null, null, pageable))
                    .isInstanceOf(TimeZoneMissingException.class);

            verify(eventPeriodResolver, never()).resolve(any(), any(), any());
            verify(eventRepository, never()).findAllByUserId(any(), any());
            verify(eventRepository, never()).findAllByUserIdAndDateRange(any(), any(), any(), any());
        }
    }
}
