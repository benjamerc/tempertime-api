package com.tempertime.tempertime_api.events.service.core;

import com.tempertime.tempertime_api.common.color.ColorGenerator;
import com.tempertime.tempertime_api.common.color.ColorValidator;
import com.tempertime.tempertime_api.common.color.InvalidColorFormatException;
import com.tempertime.tempertime_api.common.normalizer.InputNormalizer;
import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.common.pagination.PaginationValidator;
import com.tempertime.tempertime_api.events.data.EventTestDataProvider;
import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.domain.EventScope;
import com.tempertime.tempertime_api.events.domain.EventUser;
import com.tempertime.tempertime_api.events.dto.internal.TimeRange;
import com.tempertime.tempertime_api.events.dto.request.EventAssignUserRequest;
import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.request.EventUpdateRequest;
import com.tempertime.tempertime_api.events.dto.response.*;
import com.tempertime.tempertime_api.events.exception.*;
import com.tempertime.tempertime_api.events.mapper.EventMapper;
import com.tempertime.tempertime_api.events.mapper.EventUserMapper;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.events.repository.EventUserRepository;
import com.tempertime.tempertime_api.events.service.access.EventAccessService;
import com.tempertime.tempertime_api.events.service.loader.EventLoader;
import com.tempertime.tempertime_api.events.service.period.EventPeriodResolver;
import com.tempertime.tempertime_api.events.service.rules.EventDateRules;
import com.tempertime.tempertime_api.users.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.service.loader.UserLoader;
import com.tempertime.tempertime_api.workspaces.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.config.WorkspaceConstraintsProperties;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceAccessDeniedException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceEventLimitExceededException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceNotFoundException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceRoleDeniedException;
import com.tempertime.tempertime_api.workspaces.service.authorization.WorkspaceAccessService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceImplTest {

    // Repositories
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventUserRepository eventUserRepository;

    // Loaders / Services
    @Mock
    private WorkspaceAccessService workspaceAccessService;
    @Mock
    private EventAccessService eventAccessService;
    @Mock
    private EventLoader eventLoader;
    @Mock
    private UserLoader userLoader;

    // Mappers
    @Mock
    private EventMapper eventMapper;
    @Mock
    private EventUserMapper eventUserMapper;

    // Validators / Generators / Resolvers / Normalizers
    @Mock
    private ColorValidator colorValidator;
    @Mock
    private ColorGenerator colorGenerator;
    @Mock
    private EventPeriodResolver eventPeriodResolver;
    @Mock
    private EventDateRules eventDateRules;
    @Mock
    private InputNormalizer inputNormalizer;
    @Mock
    private PaginationValidator paginationValidator;

   // Configuration Properties
    @Mock
    private WorkspaceConstraintsProperties workspaceConstraintsProperties;

    // Class under test
    @InjectMocks
    private EventServiceImpl eventService;

    @Nested
    class CreateEventTests {

        @Test
        void shouldCreateGlobalEvent_whenValidDataProvided() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            EventCreateRequest request = EventTestDataProvider.eventCreateRequest();
            Event event = EventTestDataProvider.event(1L, workspace);
            EventCreateResponse response = EventTestDataProvider.eventCreateResponse(event);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(workspaceConstraintsProperties.getMaxEvents()).thenReturn(100);
            when(eventRepository.countByWorkspaceId(workspaceId)).thenReturn(0L);
            doNothing().when(eventDateRules).validateDateRange(request.eventDate());
            when(colorValidator.isColorMissing(request.color())).thenReturn(false);
            when(colorValidator.isHexColor(request.color())).thenReturn(true);
            when(inputNormalizer.normalize(request.title())).thenReturn(request.title());
            when(inputNormalizer.normalize(request.description())).thenReturn(request.description());
            when(eventRepository.save(any(Event.class))).thenReturn(event);
            doNothing().when(eventAccessService).assignGlobalEventToAllUsers(event.getId(), workspaceId);
            when(eventMapper.toEventCreateResponse(event)).thenReturn(response);

            EventCreateResponse result = eventService.createEvent(workspaceId, userId, request);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(response);

            verify(workspaceAccessService).loadWorkspaceWithOwnerAccess(workspaceId, userId);
            verify(eventDateRules).validateDateRange(request.eventDate());
            verify(colorValidator).isColorMissing(request.color());
            verify(colorValidator).isHexColor(request.color());
            verify(inputNormalizer).normalize(request.title());
            verify(inputNormalizer).normalize(request.description());
            verify(eventRepository).save(any(Event.class));
            verify(eventAccessService).assignGlobalEventToAllUsers(event.getId(), workspaceId);
            verify(eventUserRepository, never()).save(any());
            verify(eventMapper).toEventCreateResponse(event);
        }

        @Test
        void shouldCreateSpecificEvent_whenScopeIsSpecific() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);

            EventCreateRequest request = new EventCreateRequest(
                    EventTestDataProvider.TITLE,
                    OffsetDateTime.now().plusHours(1),
                    EventTestDataProvider.DESCRIPTION,
                    EventScope.SPECIFIC,
                    EventTestDataProvider.COLOR
            );

            Event event = EventTestDataProvider.specificEvent(1L, workspace);
            EventCreateResponse response = EventTestDataProvider.eventCreateResponse(event);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(workspaceConstraintsProperties.getMaxEvents()).thenReturn(100);
            when(eventRepository.countByWorkspaceId(workspaceId)).thenReturn(0L);
            doNothing().when(eventDateRules).validateDateRange(request.eventDate());
            when(colorValidator.isColorMissing(request.color())).thenReturn(false);
            when(colorValidator.isHexColor(request.color())).thenReturn(true);
            when(inputNormalizer.normalize(request.title())).thenReturn(request.title());
            when(inputNormalizer.normalize(request.description())).thenReturn(request.description());
            when(eventRepository.save(any(Event.class))).thenReturn(event);
            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);
            when(eventUserRepository.save(any(EventUser.class))).thenReturn(
                    EventTestDataProvider.eventUser(event, user)
            );
            when(eventMapper.toEventCreateResponse(event)).thenReturn(response);

            EventCreateResponse result = eventService.createEvent(workspaceId, userId, request);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(response);

            verify(eventUserRepository).save(any(EventUser.class));
            verify(userLoader).loadUserOrThrow(userId);
            verify(eventAccessService, never()).assignGlobalEventToAllUsers(any(), any());
            verify(eventMapper).toEventCreateResponse(event);
        }

        @Test
        void shouldCreateEvent_whenNoColorProvided() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            EventCreateRequest request = new EventCreateRequest(
                    EventTestDataProvider.TITLE,
                    OffsetDateTime.now().plusHours(1),
                    EventTestDataProvider.DESCRIPTION,
                    EventScope.GLOBAL,
                    null
            );

            Event event = EventTestDataProvider.event(1L, workspace);
            EventCreateResponse response = EventTestDataProvider.eventCreateResponse(event);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(workspaceConstraintsProperties.getMaxEvents()).thenReturn(100);
            when(eventRepository.countByWorkspaceId(workspaceId)).thenReturn(0L);
            doNothing().when(eventDateRules).validateDateRange(request.eventDate());
            when(colorValidator.isColorMissing(null)).thenReturn(true);
            when(colorGenerator.generate()).thenReturn(EventTestDataProvider.COLOR);
            when(inputNormalizer.normalize(request.title())).thenReturn(request.title());
            when(inputNormalizer.normalize(request.description())).thenReturn(request.description());
            when(eventRepository.save(any(Event.class))).thenReturn(event);
            doNothing().when(eventAccessService).assignGlobalEventToAllUsers(event.getId(), workspaceId);
            when(eventMapper.toEventCreateResponse(event)).thenReturn(response);

            EventCreateResponse result = eventService.createEvent(workspaceId, userId, request);

            assertThat(result).isNotNull();
            verify(colorGenerator).generate();
            verify(colorValidator, never()).isHexColor(any());
        }

        @Test
        void shouldCreateEvent_whenDescriptionIsNull() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            EventCreateRequest request = new EventCreateRequest(
                    EventTestDataProvider.TITLE,
                    OffsetDateTime.now().plusHours(1),
                    null,
                    EventScope.GLOBAL,
                    EventTestDataProvider.COLOR
            );

            Event event = EventTestDataProvider.event(1L, workspace);
            EventCreateResponse response = EventTestDataProvider.eventCreateResponse(event);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(workspaceConstraintsProperties.getMaxEvents()).thenReturn(100);
            when(eventRepository.countByWorkspaceId(workspaceId)).thenReturn(0L);
            doNothing().when(eventDateRules).validateDateRange(request.eventDate());
            when(colorValidator.isColorMissing(request.color())).thenReturn(false);
            when(colorValidator.isHexColor(request.color())).thenReturn(true);
            when(inputNormalizer.normalize(request.title())).thenReturn(request.title());
            when(inputNormalizer.normalize(null)).thenReturn(null);
            when(eventRepository.save(any(Event.class))).thenReturn(event);
            doNothing().when(eventAccessService).assignGlobalEventToAllUsers(event.getId(), workspaceId);
            when(eventMapper.toEventCreateResponse(event)).thenReturn(response);

            EventCreateResponse result = eventService.createEvent(workspaceId, userId, request);

            assertThat(result).isNotNull();
            verify(inputNormalizer).normalize(null);
        }

        @Test
        void shouldThrowInvalidColorFormatException_whenColorFormatIsInvalid() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            EventCreateRequest request = new EventCreateRequest(
                    EventTestDataProvider.TITLE,
                    OffsetDateTime.now().plusHours(1),
                    EventTestDataProvider.DESCRIPTION,
                    EventScope.GLOBAL,
                    "blue"
            );

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(workspaceConstraintsProperties.getMaxEvents()).thenReturn(100);
            when(eventRepository.countByWorkspaceId(workspaceId)).thenReturn(0L);
            doNothing().when(eventDateRules).validateDateRange(request.eventDate());
            when(colorValidator.isColorMissing("blue")).thenReturn(false);
            when(colorValidator.isHexColor("blue")).thenReturn(false);

            assertThatThrownBy(() -> eventService.createEvent(workspaceId, userId, request))
                    .isInstanceOf(InvalidColorFormatException.class);

            verify(eventRepository, never()).save(any());
            verify(eventAccessService, never()).assignGlobalEventToAllUsers(any(), any());
            verify(eventUserRepository, never()).save(any());
        }

        @Test
        void shouldThrowEventDateLimitExceededException_whenEventDateExceedsLimit() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            EventCreateRequest request = EventTestDataProvider.eventCreateRequest();

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(workspaceConstraintsProperties.getMaxEvents()).thenReturn(100);
            when(eventRepository.countByWorkspaceId(workspaceId)).thenReturn(0L);
            doThrow(new EventDateLimitExceededException())
                    .when(eventDateRules).validateDateRange(request.eventDate());

            assertThatThrownBy(() -> eventService.createEvent(workspaceId, userId, request))
                    .isInstanceOf(EventDateLimitExceededException.class);

            verify(eventRepository, never()).save(any());
            verify(eventAccessService, never()).assignGlobalEventToAllUsers(any(), any());
            verify(eventUserRepository, never()).save(any());
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            EventCreateRequest request = EventTestDataProvider.eventCreateRequest();

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> eventService.createEvent(workspaceId, userId, request))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(eventDateRules, never()).validateDateRange(any());
            verify(eventRepository, never()).save(any());
            verify(eventAccessService, never()).assignGlobalEventToAllUsers(any(), any());
            verify(eventUserRepository, never()).save(any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long userId = 2L;

            EventCreateRequest request = EventTestDataProvider.eventCreateRequest();

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() -> eventService.createEvent(workspaceId, userId, request))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(eventDateRules, never()).validateDateRange(any());
            verify(eventRepository, never()).save(any());
            verify(eventAccessService, never()).assignGlobalEventToAllUsers(any(), any());
            verify(eventUserRepository, never()).save(any());
        }

        @Test
        void shouldThrowWorkspaceEventLimitExceededException_whenWorkspaceEventLimitIsReached() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            EventCreateRequest request = EventTestDataProvider.eventCreateRequest();

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(workspaceConstraintsProperties.getMaxEvents()).thenReturn(100);
            when(eventRepository.countByWorkspaceId(workspaceId)).thenReturn(100L);

            assertThatThrownBy(() -> eventService.createEvent(workspaceId, userId, request))
                    .isInstanceOf(WorkspaceEventLimitExceededException.class);

            verify(eventDateRules, never()).validateDateRange(any());
            verify(eventRepository, never()).save(any());
            verify(eventAccessService, never()).assignGlobalEventToAllUsers(any(), any());
            verify(eventUserRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateEventTests {

        @Test
        void shouldUpdateAllFields_whenValidDataProvided() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);

            EventUpdateRequest request = new EventUpdateRequest(
                    "UpdatedTitle",
                    OffsetDateTime.now().plusHours(2),
                    "UpdatedDescription",
                    "#B2C3D4"
            );

            EventResponse response = EventTestDataProvider.eventResponse(event);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(inputNormalizer.normalize("UpdatedTitle")).thenReturn("UpdatedTitle");
            when(inputNormalizer.normalize("UpdatedDescription")).thenReturn("UpdatedDescription");
            doNothing().when(eventDateRules).validateDateRange(request.eventDate());
            when(colorValidator.isColorMissing("#B2C3D4")).thenReturn(false);
            when(colorValidator.isHexColor("#B2C3D4")).thenReturn(true);
            when(eventRepository.save(event)).thenReturn(event);
            when(eventMapper.toEventResponse(event)).thenReturn(response);

            EventResponse result = eventService.updateEvent(workspaceId, eventId, userId, request);

            assertThat(result).isNotNull();
            assertThat(event.getTitle()).isEqualTo("UpdatedTitle");
            assertThat(event.getDescription()).isEqualTo("UpdatedDescription");
            assertThat(event.getColor()).isEqualTo("#B2C3D4");

            verify(workspaceAccessService).loadWorkspaceWithOwnerAccess(workspaceId, userId);
            verify(eventLoader).loadOrThrow(workspaceId, eventId);
            verify(inputNormalizer).normalize("UpdatedTitle");
            verify(inputNormalizer).normalize("UpdatedDescription");
            verify(eventDateRules).validateDateRange(request.eventDate());
            verify(colorValidator).isColorMissing("#B2C3D4");
            verify(colorValidator).isHexColor("#B2C3D4");
            verify(eventRepository).save(event);
            verify(eventMapper).toEventResponse(event);
        }

        @Test
        void shouldUpdateOnlyTitle_whenOtherFieldsAreNull() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);
            String originalDescription = event.getDescription();
            String originalColor = event.getColor();

            EventUpdateRequest request = new EventUpdateRequest("UpdatedTitle", null, null, null);
            EventResponse response = EventTestDataProvider.eventResponse(event);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(inputNormalizer.normalize("UpdatedTitle")).thenReturn("UpdatedTitle");
            when(eventRepository.save(event)).thenReturn(event);
            when(eventMapper.toEventResponse(event)).thenReturn(response);

            eventService.updateEvent(workspaceId, eventId, userId, request);

            assertThat(event.getTitle()).isEqualTo("UpdatedTitle");
            assertThat(event.getDescription()).isEqualTo(originalDescription);
            assertThat(event.getColor()).isEqualTo(originalColor);

            verify(inputNormalizer).normalize("UpdatedTitle");
            verify(eventDateRules, never()).validateDateRange(any());
            verify(colorValidator, never()).isColorMissing(any());
            verify(eventRepository).save(event);
        }

        @Test
        void shouldUpdateOnlyDescription_whenOtherFieldsAreNull() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);
            String originalTitle = event.getTitle();
            String originalColor = event.getColor();

            EventUpdateRequest request = new EventUpdateRequest(null, null, "UpdatedDescription", null);
            EventResponse response = EventTestDataProvider.eventResponse(event);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(inputNormalizer.normalize("UpdatedDescription")).thenReturn("UpdatedDescription");
            when(eventRepository.save(event)).thenReturn(event);
            when(eventMapper.toEventResponse(event)).thenReturn(response);

            eventService.updateEvent(workspaceId, eventId, userId, request);

            assertThat(event.getTitle()).isEqualTo(originalTitle);
            assertThat(event.getDescription()).isEqualTo("UpdatedDescription");
            assertThat(event.getColor()).isEqualTo(originalColor);

            verify(inputNormalizer).normalize("UpdatedDescription");
            verify(eventDateRules, never()).validateDateRange(any());
            verify(colorValidator, never()).isColorMissing(any());
            verify(eventRepository).save(event);
        }

        @Test
        void shouldUpdateOnlyEventDate_whenOtherFieldsAreNull() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);
            String originalTitle = event.getTitle();

            OffsetDateTime newDate = OffsetDateTime.now().plusHours(2);
            EventUpdateRequest request = new EventUpdateRequest(null, newDate, null, null);
            EventResponse response = EventTestDataProvider.eventResponse(event);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            doNothing().when(eventDateRules).validateDateRange(newDate);
            when(eventRepository.save(event)).thenReturn(event);
            when(eventMapper.toEventResponse(event)).thenReturn(response);

            eventService.updateEvent(workspaceId, eventId, userId, request);

            assertThat(event.getTitle()).isEqualTo(originalTitle);
            assertThat(event.getEventDate()).isEqualTo(newDate.toInstant());

            verify(eventDateRules).validateDateRange(newDate);
            verify(inputNormalizer, never()).normalize(any());
            verify(colorValidator, never()).isColorMissing(any());
            verify(eventRepository).save(event);
        }

        @Test
        void shouldUpdateOnlyColor_whenOtherFieldsAreNull() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);
            String originalTitle = event.getTitle();

            EventUpdateRequest request = new EventUpdateRequest(null, null, null, "#B2C3D4");
            EventResponse response = EventTestDataProvider.eventResponse(event);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(colorValidator.isColorMissing("#B2C3D4")).thenReturn(false);
            when(colorValidator.isHexColor("#B2C3D4")).thenReturn(true);
            when(eventRepository.save(event)).thenReturn(event);
            when(eventMapper.toEventResponse(event)).thenReturn(response);

            eventService.updateEvent(workspaceId, eventId, userId, request);

            assertThat(event.getTitle()).isEqualTo(originalTitle);
            assertThat(event.getColor()).isEqualTo("#B2C3D4");

            verify(colorValidator).isColorMissing("#B2C3D4");
            verify(colorValidator).isHexColor("#B2C3D4");
            verify(inputNormalizer, never()).normalize(any());
            verify(eventDateRules, never()).validateDateRange(any());
            verify(eventRepository).save(event);
        }

        @Test
        void shouldNotUpdateTitle_whenTitleIsBlank() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);
            String originalTitle = event.getTitle();

            EventUpdateRequest request = new EventUpdateRequest("   ", null, null, null);
            EventResponse response = EventTestDataProvider.eventResponse(event);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventRepository.save(event)).thenReturn(event);
            when(eventMapper.toEventResponse(event)).thenReturn(response);

            eventService.updateEvent(workspaceId, eventId, userId, request);

            assertThat(event.getTitle()).isEqualTo(originalTitle);

            verify(inputNormalizer, never()).normalize(any());
            verify(eventRepository).save(event);
        }

        @Test
        void shouldNotUpdateDescription_whenDescriptionIsBlank() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);
            String originalDescription = event.getDescription();

            EventUpdateRequest request = new EventUpdateRequest(null, null, "   ", null);
            EventResponse response = EventTestDataProvider.eventResponse(event);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventRepository.save(event)).thenReturn(event);
            when(eventMapper.toEventResponse(event)).thenReturn(response);

            eventService.updateEvent(workspaceId, eventId, userId, request);

            assertThat(event.getDescription()).isEqualTo(originalDescription);

            verify(inputNormalizer, never()).normalize(any());
            verify(eventRepository).save(event);
        }

        @Test
        void shouldNotUpdateColor_whenColorIsBlank() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);
            String originalColor = event.getColor();

            EventUpdateRequest request = new EventUpdateRequest(null, null, null, "   ");
            EventResponse response = EventTestDataProvider.eventResponse(event);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventRepository.save(event)).thenReturn(event);
            when(eventMapper.toEventResponse(event)).thenReturn(response);

            eventService.updateEvent(workspaceId, eventId, userId, request);

            assertThat(event.getColor()).isEqualTo(originalColor);

            verify(colorValidator, never()).isColorMissing(any());
            verify(eventRepository).save(event);
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long eventId = 1L;
            Long userId = 1L;

            EventUpdateRequest request = new EventUpdateRequest("UpdatedTitle", null, null, null);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> eventService.updateEvent(workspaceId, eventId, userId, request))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(eventLoader, never()).loadOrThrow(any(), any());
            verify(eventRepository, never()).save(any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 2L;

            EventUpdateRequest request = new EventUpdateRequest("UpdatedTitle", null, null, null);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() -> eventService.updateEvent(workspaceId, eventId, userId, request))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(eventLoader, never()).loadOrThrow(any(), any());
            verify(eventRepository, never()).save(any());
        }

        @Test
        void shouldThrowEventNotFoundException_whenEventDoesNotExist() {

            Long workspaceId = 1L;
            Long eventId = 99L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            EventUpdateRequest request = new EventUpdateRequest("UpdatedTitle", null, null, null);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId))
                    .thenThrow(new EventNotFoundException());

            assertThatThrownBy(() -> eventService.updateEvent(workspaceId, eventId, userId, request))
                    .isInstanceOf(EventNotFoundException.class);

            verify(eventRepository, never()).save(any());
        }

        @Test
        void shouldThrowEventDateLimitExceededException_whenEventDateExceedsLimit() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);

            OffsetDateTime invalidDate = OffsetDateTime.now().plusYears(5);
            EventUpdateRequest request = new EventUpdateRequest(null, invalidDate, null, null);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            doThrow(new EventDateLimitExceededException())
                    .when(eventDateRules).validateDateRange(invalidDate);

            assertThatThrownBy(() -> eventService.updateEvent(workspaceId, eventId, userId, request))
                    .isInstanceOf(EventDateLimitExceededException.class);

            verify(eventRepository, never()).save(any());
        }

        @Test
        void shouldThrowInvalidColorFormatException_whenColorFormatIsInvalid() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);

            EventUpdateRequest request = new EventUpdateRequest(null, null, null, "blue");

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(colorValidator.isColorMissing("blue")).thenReturn(false);
            when(colorValidator.isHexColor("blue")).thenReturn(false);

            assertThatThrownBy(() -> eventService.updateEvent(workspaceId, eventId, userId, request))
                    .isInstanceOf(InvalidColorFormatException.class);

            verify(eventRepository, never()).save(any());
        }
    }

    @Nested
    class GetEventsTests {

        @Test
        void shouldReturnPagedEvents_whenPeriodIsAll() {

            Long workspaceId = 1L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(1L, workspace);
            EventListItemResponse listItemResponse = EventTestDataProvider.eventListItemResponse(event);
            Page<Event> page = new PageImpl<>(List.of(event));

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, UserTestDataProvider.user(userId)));
            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventRepository.findEventsByWorkspaceAndUser(workspaceId, userId, pageable))
                    .thenReturn(page);
            when(eventMapper.toEventListItemResponse(event)).thenReturn(listItemResponse);

            PageResponse<EventListItemResponse> result = eventService.getEvents(
                    workspaceId, userId, EventPeriod.ALL, null, null, pageable
            );

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0)).usingRecursiveComparison().isEqualTo(listItemResponse);

            verify(workspaceAccessService).requireAccessibleWorkspace(workspaceId, userId);
            verify(paginationValidator).validate(pageable);
            verify(eventRepository).findEventsByWorkspaceAndUser(workspaceId, userId, pageable);
            verify(eventPeriodResolver, never()).resolve(any(), any(), any());
            verify(eventRepository, never()).findEventsByWorkspaceAndUserAndDateRange(any(), any(), any(), any(), any());
        }

        @Test
        void shouldReturnPagedEvents_whenPeriodIsDayAndTimeZoneProvided() {

            Long workspaceId = 1L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();
            ZoneId zone = ZoneId.of("America/Argentina/Buenos_Aires");

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(1L, workspace);
            EventListItemResponse listItemResponse = EventTestDataProvider.eventListItemResponse(event);

            Instant start = Instant.now().minusSeconds(3600);
            Instant end = Instant.now().plusSeconds(3600);
            TimeRange range = new TimeRange(start, end);
            Page<Event> page = new PageImpl<>(List.of(event));

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, UserTestDataProvider.user(userId)));
            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventPeriodResolver.resolve(eq(EventPeriod.DAY), eq(zone), any()))
                    .thenReturn(Optional.of(range));
            when(eventRepository.findEventsByWorkspaceAndUserAndDateRange(
                    workspaceId, userId, start, end, pageable))
                    .thenReturn(page);
            when(eventMapper.toEventListItemResponse(event)).thenReturn(listItemResponse);

            PageResponse<EventListItemResponse> result = eventService.getEvents(
                    workspaceId, userId, EventPeriod.DAY, zone, null, pageable
            );

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);

            verify(eventPeriodResolver).resolve(eq(EventPeriod.DAY), eq(zone), any());
            verify(eventRepository).findEventsByWorkspaceAndUserAndDateRange(
                    workspaceId, userId, start, end, pageable);
            verify(eventRepository, never()).findEventsByWorkspaceAndUser(any(), any(), any());
        }

        @Test
        void shouldReturnPagedEvents_whenPeriodIsAllAndDateIsProvided() {

            Long workspaceId = 1L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();
            LocalDate date = LocalDate.of(2026, 5, 2);

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(1L, workspace);
            EventListItemResponse listItemResponse = EventTestDataProvider.eventListItemResponse(event);
            Page<Event> page = new PageImpl<>(List.of(event));

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, UserTestDataProvider.user(userId)));
            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventRepository.findEventsByWorkspaceAndUser(workspaceId, userId, pageable))
                    .thenReturn(page);
            when(eventMapper.toEventListItemResponse(event)).thenReturn(listItemResponse);

            PageResponse<EventListItemResponse> result = eventService.getEvents(
                    workspaceId, userId, EventPeriod.ALL, null, date, pageable
            );

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);

            verify(eventPeriodResolver, never()).resolve(any(), any(), any());
            verify(eventRepository).findEventsByWorkspaceAndUser(workspaceId, userId, pageable);
            verify(eventRepository, never()).findEventsByWorkspaceAndUserAndDateRange(any(), any(), any(), any(), any());
        }

        @Test
        void shouldReturnPagedEvents_whenPeriodIsWeekAndBaseDateProvided() {

            Long workspaceId = 1L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();
            ZoneId zone = ZoneId.of("America/Argentina/Buenos_Aires");
            LocalDate date = LocalDate.of(2026, 5, 2);

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(1L, workspace);
            EventListItemResponse listItemResponse = EventTestDataProvider.eventListItemResponse(event);

            Instant start = Instant.now().minusSeconds(3600);
            Instant end = Instant.now().plusSeconds(3600);
            TimeRange range = new TimeRange(start, end);
            Page<Event> page = new PageImpl<>(List.of(event));

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, UserTestDataProvider.user(userId)));
            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventPeriodResolver.resolve(eq(EventPeriod.WEEK), eq(zone), any()))
                    .thenReturn(Optional.of(range));
            when(eventRepository.findEventsByWorkspaceAndUserAndDateRange(
                    workspaceId, userId, start, end, pageable))
                    .thenReturn(page);
            when(eventMapper.toEventListItemResponse(event)).thenReturn(listItemResponse);

            PageResponse<EventListItemResponse> result = eventService.getEvents(
                    workspaceId, userId, EventPeriod.WEEK, zone, date, pageable
            );

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);

            verify(eventPeriodResolver).resolve(eq(EventPeriod.WEEK), eq(zone), any());
            verify(eventRepository).findEventsByWorkspaceAndUserAndDateRange(
                    workspaceId, userId, start, end, pageable);
        }

        @Test
        void shouldReturnEmptyPage_whenUserHasNoEventsInWorkspace() {

            Long workspaceId = 1L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, UserTestDataProvider.user(userId)));
            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventRepository.findEventsByWorkspaceAndUser(workspaceId, userId, pageable))
                    .thenReturn(Page.empty());

            PageResponse<EventListItemResponse> result = eventService.getEvents(
                    workspaceId, userId, EventPeriod.ALL, null, null, pageable
            );

            assertThat(result.content()).isEmpty();

            verify(eventMapper, never()).toEventListItemResponse(any());
        }

        @Test
        void shouldThrowTimeZoneMissingException_whenPeriodIsNotAllAndTimeZoneIsNull() {

            Long workspaceId = 1L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, UserTestDataProvider.user(userId)));
            when(paginationValidator.validate(pageable)).thenReturn(pageable);

            assertThatThrownBy(() -> eventService.getEvents(
                    workspaceId, userId, EventPeriod.DAY, null, null, pageable))
                    .isInstanceOf(TimeZoneMissingException.class);

            verify(eventPeriodResolver, never()).resolve(any(), any(), any());
            verify(eventRepository, never()).findEventsByWorkspaceAndUser(any(), any(), any());
            verify(eventRepository, never()).findEventsByWorkspaceAndUserAndDateRange(any(), any(), any(), any(), any());
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> eventService.getEvents(
                    workspaceId, userId, EventPeriod.ALL, null, null, pageable))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(paginationValidator, never()).validate(any());
            verify(eventRepository, never()).findEventsByWorkspaceAndUser(any(), any(), any());
        }

        @Test
        void shouldThrowWorkspaceAccessDeniedException_whenUserIsNotInWorkspace() {

            Long workspaceId = 1L;
            Long userId = 99L;
            Pageable pageable = Pageable.unpaged();

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenThrow(new WorkspaceAccessDeniedException());

            assertThatThrownBy(() -> eventService.getEvents(
                    workspaceId, userId, EventPeriod.ALL, null, null, pageable))
                    .isInstanceOf(WorkspaceAccessDeniedException.class);

            verify(paginationValidator, never()).validate(any());
            verify(eventRepository, never()).findEventsByWorkspaceAndUser(any(), any(), any());
        }
    }

    @Nested
    class GetEventTests {

        @Test
        void shouldReturnEvent_whenUserHasAccess() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);
            EventResponse response = EventTestDataProvider.eventResponse(event);

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, UserTestDataProvider.user(userId)));
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventUserRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);
            when(eventMapper.toEventResponse(event)).thenReturn(response);

            EventResponse result = eventService.getEvent(workspaceId, eventId, userId);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(response);

            verify(workspaceAccessService).requireAccessibleWorkspace(workspaceId, userId);
            verify(eventLoader).loadOrThrow(workspaceId, eventId);
            verify(eventUserRepository).existsByEventIdAndUserId(eventId, userId);
            verify(eventMapper).toEventResponse(event);
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long eventId = 1L;
            Long userId = 1L;

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> eventService.getEvent(workspaceId, eventId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(eventLoader, never()).loadOrThrow(any(), any());
            verify(eventUserRepository, never()).existsByEventIdAndUserId(any(), any());
            verify(eventMapper, never()).toEventResponse(any());
        }

        @Test
        void shouldThrowWorkspaceAccessDeniedException_whenUserIsNotInWorkspace() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 99L;

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenThrow(new WorkspaceAccessDeniedException());

            assertThatThrownBy(() -> eventService.getEvent(workspaceId, eventId, userId))
                    .isInstanceOf(WorkspaceAccessDeniedException.class);

            verify(eventLoader, never()).loadOrThrow(any(), any());
            verify(eventUserRepository, never()).existsByEventIdAndUserId(any(), any());
            verify(eventMapper, never()).toEventResponse(any());
        }

        @Test
        void shouldThrowEventNotFoundException_whenEventDoesNotExist() {

            Long workspaceId = 1L;
            Long eventId = 99L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, UserTestDataProvider.user(userId)));
            when(eventLoader.loadOrThrow(workspaceId, eventId))
                    .thenThrow(new EventNotFoundException());

            assertThatThrownBy(() -> eventService.getEvent(workspaceId, eventId, userId))
                    .isInstanceOf(EventNotFoundException.class);

            verify(eventUserRepository, never()).existsByEventIdAndUserId(any(), any());
            verify(eventMapper, never()).toEventResponse(any());
        }

        @Test
        void shouldThrowEventAccessDeniedException_whenUserIsNotAssignedToEvent() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 2L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.memberWorkspaceUser(workspace, UserTestDataProvider.user(userId)));
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventUserRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);

            assertThatThrownBy(() -> eventService.getEvent(workspaceId, eventId, userId))
                    .isInstanceOf(EventAccessDeniedException.class);

            verify(eventMapper, never()).toEventResponse(any());
        }
    }

    @Nested
    class DeleteEventTests {

        @Test
        void shouldDeleteEvent_whenUserIsOwner() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            doNothing().when(eventUserRepository).deleteByEventId(eventId);
            doNothing().when(eventRepository).deleteById(eventId);

            eventService.deleteEvent(workspaceId, eventId, userId);

            InOrder inOrder = inOrder(eventUserRepository, eventRepository);
            inOrder.verify(eventUserRepository).deleteByEventId(eventId);
            inOrder.verify(eventRepository).deleteById(eventId);

            verify(workspaceAccessService).loadWorkspaceWithOwnerAccess(workspaceId, userId);
            verify(eventLoader).loadOrThrow(workspaceId, eventId);
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long eventId = 1L;
            Long userId = 1L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> eventService.deleteEvent(workspaceId, eventId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(eventLoader, never()).loadOrThrow(any(), any());
            verify(eventUserRepository, never()).deleteByEventId(any());
            verify(eventRepository, never()).deleteById(any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 2L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() -> eventService.deleteEvent(workspaceId, eventId, userId))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(eventLoader, never()).loadOrThrow(any(), any());
            verify(eventUserRepository, never()).deleteByEventId(any());
            verify(eventRepository, never()).deleteById(any());
        }

        @Test
        void shouldThrowEventNotFoundException_whenEventDoesNotExist() {

            Long workspaceId = 1L;
            Long eventId = 99L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId))
                    .thenThrow(new EventNotFoundException());

            assertThatThrownBy(() -> eventService.deleteEvent(workspaceId, eventId, userId))
                    .isInstanceOf(EventNotFoundException.class);

            verify(eventUserRepository, never()).deleteByEventId(any());
            verify(eventRepository, never()).deleteById(any());
        }
    }

    @Nested
    class AssignUsersToEventTests {

        @Test
        void shouldAssignUsersToEvent_whenUsersAreNotAlreadyAssigned() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;
            Long targetUserId = 2L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User targetUser = UserTestDataProvider.user(targetUserId);
            Event event = EventTestDataProvider.specificEvent(eventId, workspace);

            EventAssignUserRequest request = new EventAssignUserRequest(List.of(targetUserId));

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventUserRepository.findAllByEventId(eventId)).thenReturn(List.of());
            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, targetUserId))
                    .thenReturn(WorkspaceTestDataProvider.memberWorkspaceUser(workspace, targetUser));
            when(userLoader.loadUserOrThrow(targetUserId)).thenReturn(targetUser);
            when(eventUserRepository.saveAll(anyList())).thenReturn(List.of());
            when(eventUserRepository.countByEventId(eventId)).thenReturn(2L);
            when(eventRepository.save(event)).thenReturn(event);

            EventAssignUserResponse result =
                    eventService.assignUsersToEvent(workspaceId, eventId, userId, request);

            assertThat(result).isNotNull();
            assertThat(result.eventId()).isEqualTo(eventId);
            assertThat(result.userIds()).containsExactly(targetUserId);
            assertThat(event.getHasActiveUsers()).isTrue();

            verify(workspaceAccessService).loadWorkspaceWithOwnerAccess(workspaceId, userId);
            verify(eventLoader).loadOrThrow(workspaceId, eventId);
            verify(eventUserRepository).findAllByEventId(eventId);
            verify(workspaceAccessService).requireAccessibleWorkspace(workspaceId, targetUserId);
            verify(userLoader).loadUserOrThrow(targetUserId);
            verify(eventUserRepository).saveAll(anyList());
            verify(eventUserRepository).countByEventId(eventId);
            verify(eventRepository).save(event);
        }

        @Test
        void shouldSkipAlreadyAssignedUsers_whenSomeUsersAreAlreadyAssigned() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;
            Long alreadyAssignedUserId = 2L;
            Long newUserId = 3L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User alreadyAssignedUser = UserTestDataProvider.user(alreadyAssignedUserId);
            User newUser = UserTestDataProvider.user(newUserId);
            Event event = EventTestDataProvider.specificEvent(eventId, workspace);
            EventUser existingEventUser = EventTestDataProvider.eventUser(event, alreadyAssignedUser);

            EventAssignUserRequest request =
                    new EventAssignUserRequest(List.of(alreadyAssignedUserId, newUserId));

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventUserRepository.findAllByEventId(eventId)).thenReturn(List.of(existingEventUser));
            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, alreadyAssignedUserId))
                    .thenReturn(WorkspaceTestDataProvider.memberWorkspaceUser(workspace, alreadyAssignedUser));
            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, newUserId))
                    .thenReturn(WorkspaceTestDataProvider.memberWorkspaceUser(workspace, newUser));
            when(userLoader.loadUserOrThrow(newUserId)).thenReturn(newUser);
            when(eventUserRepository.saveAll(anyList())).thenReturn(List.of());
            when(eventUserRepository.countByEventId(eventId)).thenReturn(2L);
            when(eventRepository.save(event)).thenReturn(event);

            EventAssignUserResponse result =
                    eventService.assignUsersToEvent(workspaceId, eventId, userId, request);

            assertThat(result.userIds()).containsExactly(newUserId);
            assertThat(result.userIds()).doesNotContain(alreadyAssignedUserId);

            verify(userLoader, never()).loadUserOrThrow(alreadyAssignedUserId);
            verify(userLoader).loadUserOrThrow(newUserId);
        }

        @Test
        void shouldThrowEventNotAssignableException_whenEventScopeIsGlobal() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);
            EventAssignUserRequest request = new EventAssignUserRequest(List.of(2L));

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);

            assertThatThrownBy(() ->
                    eventService.assignUsersToEvent(workspaceId, eventId, userId, request))
                    .isInstanceOf(EventNotAssignableException.class);

            verify(eventUserRepository, never()).findAllByEventId(any());
            verify(eventUserRepository, never()).saveAll(anyList());
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long eventId = 1L;
            Long userId = 1L;

            EventAssignUserRequest request = new EventAssignUserRequest(List.of(2L));

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() ->
                    eventService.assignUsersToEvent(workspaceId, eventId, userId, request))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(eventLoader, never()).loadOrThrow(any(), any());
            verify(eventUserRepository, never()).saveAll(anyList());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 2L;

            EventAssignUserRequest request = new EventAssignUserRequest(List.of(3L));

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() ->
                    eventService.assignUsersToEvent(workspaceId, eventId, userId, request))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(eventLoader, never()).loadOrThrow(any(), any());
            verify(eventUserRepository, never()).saveAll(anyList());
        }

        @Test
        void shouldThrowEventNotFoundException_whenEventDoesNotExist() {

            Long workspaceId = 1L;
            Long eventId = 99L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            EventAssignUserRequest request = new EventAssignUserRequest(List.of(2L));

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId))
                    .thenThrow(new EventNotFoundException());

            assertThatThrownBy(() ->
                    eventService.assignUsersToEvent(workspaceId, eventId, userId, request))
                    .isInstanceOf(EventNotFoundException.class);

            verify(eventUserRepository, never()).findAllByEventId(any());
            verify(eventUserRepository, never()).saveAll(anyList());
        }

        @Test
        void shouldThrowWorkspaceAccessDeniedException_whenTargetUserIsNotInWorkspace() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;
            Long targetUserId = 99L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.specificEvent(eventId, workspace);
            EventAssignUserRequest request = new EventAssignUserRequest(List.of(targetUserId));

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventUserRepository.findAllByEventId(eventId)).thenReturn(List.of());
            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, targetUserId))
                    .thenThrow(new WorkspaceAccessDeniedException());

            assertThatThrownBy(() ->
                    eventService.assignUsersToEvent(workspaceId, eventId, userId, request))
                    .isInstanceOf(WorkspaceAccessDeniedException.class);

            verify(userLoader, never()).loadUserOrThrow(any());
            verify(eventUserRepository, never()).saveAll(anyList());
        }
    }

    @Nested
    class GetEventAssignedUsersTests {

        @Test
        void shouldReturnPagedAssignedUsers_whenUserHasAccess() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            Event event = EventTestDataProvider.event(eventId, workspace);
            EventUser eventUser = EventTestDataProvider.eventUser(event, user);
            EventAssignedUserResponse assignedUserResponse =
                    EventTestDataProvider.eventAssignedUserResponse(user);

            Page<EventUser> page = new PageImpl<>(List.of(eventUser));

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, user));
            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventUserRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);
            when(eventUserRepository.findAllByEventId(eventId, pageable)).thenReturn(page);
            when(eventUserMapper.toEventAssignedUserResponse(eventUser)).thenReturn(assignedUserResponse);

            PageResponse<EventAssignedUserResponse> result =
                    eventService.getEventAssignedUsers(workspaceId, eventId, userId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0)).usingRecursiveComparison().isEqualTo(assignedUserResponse);

            verify(workspaceAccessService).requireAccessibleWorkspace(workspaceId, userId);
            verify(paginationValidator).validate(pageable);
            verify(eventLoader).loadOrThrow(workspaceId, eventId);
            verify(eventUserRepository).existsByEventIdAndUserId(eventId, userId);
            verify(eventUserRepository).findAllByEventId(eventId, pageable);
            verify(eventUserMapper).toEventAssignedUserResponse(eventUser);
        }

        @Test
        void shouldReturnEmptyPage_whenEventHasNoAssignedUsers() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            Event event = EventTestDataProvider.event(eventId, workspace);

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, user));
            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventUserRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(true);
            when(eventUserRepository.findAllByEventId(eventId, pageable)).thenReturn(Page.empty());

            PageResponse<EventAssignedUserResponse> result =
                    eventService.getEventAssignedUsers(workspaceId, eventId, userId, pageable);

            assertThat(result.content()).isEmpty();

            verify(eventUserMapper, never()).toEventAssignedUserResponse(any());
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long eventId = 1L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() ->
                    eventService.getEventAssignedUsers(workspaceId, eventId, userId, pageable))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(paginationValidator, never()).validate(any());
            verify(eventLoader, never()).loadOrThrow(any(), any());
            verify(eventUserRepository, never()).findAllByEventId(any(), any(Pageable.class));
        }

        @Test
        void shouldThrowWorkspaceAccessDeniedException_whenUserIsNotInWorkspace() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 99L;
            Pageable pageable = Pageable.unpaged();

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenThrow(new WorkspaceAccessDeniedException());

            assertThatThrownBy(() ->
                    eventService.getEventAssignedUsers(workspaceId, eventId, userId, pageable))
                    .isInstanceOf(WorkspaceAccessDeniedException.class);

            verify(paginationValidator, never()).validate(any());
            verify(eventLoader, never()).loadOrThrow(any(), any());
            verify(eventUserRepository, never()).findAllByEventId(any(), any(Pageable.class));
        }

        @Test
        void shouldThrowEventNotFoundException_whenEventDoesNotExist() {

            Long workspaceId = 1L;
            Long eventId = 99L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, user));
            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventLoader.loadOrThrow(workspaceId, eventId))
                    .thenThrow(new EventNotFoundException());

            assertThatThrownBy(() ->
                    eventService.getEventAssignedUsers(workspaceId, eventId, userId, pageable))
                    .isInstanceOf(EventNotFoundException.class);

            verify(eventUserRepository, never()).existsByEventIdAndUserId(any(), any());
            verify(eventUserRepository, never()).findAllByEventId(any(), any(Pageable.class));
        }

        @Test
        void shouldThrowEventAccessDeniedException_whenUserIsNotAssignedToEvent() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long userId = 2L;
            Pageable pageable = Pageable.unpaged();

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            Event event = EventTestDataProvider.event(eventId, workspace);

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenReturn(WorkspaceTestDataProvider.memberWorkspaceUser(workspace, user));
            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventUserRepository.existsByEventIdAndUserId(eventId, userId)).thenReturn(false);

            assertThatThrownBy(() ->
                    eventService.getEventAssignedUsers(workspaceId, eventId, userId, pageable))
                    .isInstanceOf(EventAccessDeniedException.class);

            verify(eventUserRepository, never()).findAllByEventId(any(), any(Pageable.class));
            verify(eventUserMapper, never()).toEventAssignedUserResponse(any());
        }
    }

    @Nested
    class DeleteUserFromEventTests {

        @Test
        void shouldDeleteUserFromEvent_whenUserIsOwnerAndTargetIsAssigned() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long targetUserId = 2L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.specificEvent(eventId, workspace);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventUserRepository.existsByEventIdAndUserId(eventId, targetUserId)).thenReturn(true);
            doNothing().when(eventUserRepository).deleteByEventIdAndUserId(eventId, targetUserId);
            when(eventUserRepository.countByEventId(eventId)).thenReturn(1L);
            when(eventRepository.save(event)).thenReturn(event);

            eventService.deleteUserFromEvent(workspaceId, eventId, targetUserId, userId);

            assertThat(event.getHasActiveUsers()).isFalse();

            verify(workspaceAccessService).loadWorkspaceWithOwnerAccess(workspaceId, userId);
            verify(eventLoader).loadOrThrow(workspaceId, eventId);
            verify(eventUserRepository).existsByEventIdAndUserId(eventId, targetUserId);
            verify(eventUserRepository).deleteByEventIdAndUserId(eventId, targetUserId);
            verify(eventUserRepository).countByEventId(eventId);
            verify(eventRepository).save(event);
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long eventId = 1L;
            Long targetUserId = 2L;
            Long userId = 1L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() ->
                    eventService.deleteUserFromEvent(workspaceId, eventId, targetUserId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(eventLoader, never()).loadOrThrow(any(), any());
            verify(eventUserRepository, never()).existsByEventIdAndUserId(any(), any());
            verify(eventUserRepository, never()).deleteByEventIdAndUserId(any(), any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long targetUserId = 2L;
            Long userId = 3L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() ->
                    eventService.deleteUserFromEvent(workspaceId, eventId, targetUserId, userId))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(eventLoader, never()).loadOrThrow(any(), any());
            verify(eventUserRepository, never()).existsByEventIdAndUserId(any(), any());
            verify(eventUserRepository, never()).deleteByEventIdAndUserId(any(), any());
        }

        @Test
        void shouldThrowEventNotFoundException_whenEventDoesNotExist() {

            Long workspaceId = 1L;
            Long eventId = 99L;
            Long targetUserId = 2L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId))
                    .thenThrow(new EventNotFoundException());

            assertThatThrownBy(() ->
                    eventService.deleteUserFromEvent(workspaceId, eventId, targetUserId, userId))
                    .isInstanceOf(EventNotFoundException.class);

            verify(eventUserRepository, never()).existsByEventIdAndUserId(any(), any());
            verify(eventUserRepository, never()).deleteByEventIdAndUserId(any(), any());
        }

        @Test
        void shouldThrowEventNotAssignableException_whenEventScopeIsGlobal() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long targetUserId = 2L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(eventId, workspace);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);

            assertThatThrownBy(() ->
                    eventService.deleteUserFromEvent(workspaceId, eventId, targetUserId, userId))
                    .isInstanceOf(EventNotAssignableException.class);

            verify(eventUserRepository, never()).existsByEventIdAndUserId(any(), any());
            verify(eventUserRepository, never()).deleteByEventIdAndUserId(any(), any());
        }

        @Test
        void shouldThrowUserNotAssignedToEventException_whenTargetUserIsNotAssigned() {

            Long workspaceId = 1L;
            Long eventId = 1L;
            Long targetUserId = 99L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.specificEvent(eventId, workspace);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenReturn(workspace);
            when(eventLoader.loadOrThrow(workspaceId, eventId)).thenReturn(event);
            when(eventUserRepository.existsByEventIdAndUserId(eventId, targetUserId)).thenReturn(false);

            assertThatThrownBy(() ->
                    eventService.deleteUserFromEvent(workspaceId, eventId, targetUserId, userId))
                    .isInstanceOf(UserNotAssignedToEventException.class);

            verify(eventUserRepository, never()).deleteByEventIdAndUserId(any(), any());
            verify(eventRepository, never()).save(any());
        }
    }
}
