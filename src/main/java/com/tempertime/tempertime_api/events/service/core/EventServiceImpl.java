package com.tempertime.tempertime_api.events.service.core;

import com.tempertime.tempertime_api.common.color.ColorGenerator;
import com.tempertime.tempertime_api.common.color.ColorUtil;
import com.tempertime.tempertime_api.common.color.ColorValidator;
import com.tempertime.tempertime_api.common.normalizer.InputNormalizer;
import com.tempertime.tempertime_api.common.pagination.PageMapper;
import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.common.pagination.PaginationValidator;
import com.tempertime.tempertime_api.events.dto.internal.TimeRange;
import com.tempertime.tempertime_api.events.dto.request.EventAssignUserRequest;
import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.request.EventUpdateRequest;
import com.tempertime.tempertime_api.events.dto.response.*;
import com.tempertime.tempertime_api.events.exception.*;
import com.tempertime.tempertime_api.events.service.period.EventPeriodResolver;
import com.tempertime.tempertime_api.events.mapper.EventMapper;
import com.tempertime.tempertime_api.events.mapper.EventUserMapper;
import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.domain.EventScope;
import com.tempertime.tempertime_api.events.domain.EventUser;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.events.repository.EventUserRepository;
import com.tempertime.tempertime_api.events.service.loader.EventLoader;
import com.tempertime.tempertime_api.events.service.rules.EventDateRules;
import com.tempertime.tempertime_api.users.service.loader.UserLoader;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceAccessDeniedException;
import com.tempertime.tempertime_api.workspaces.service.authorization.WorkspaceAccessService;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    // Repositories
    private final EventRepository eventRepository;
    private final EventUserRepository eventUserRepository;

    // Loaders / Services
    private final WorkspaceAccessService workspaceAccessService;
    private final EventLoader eventLoader;
    private final UserLoader userLoader;

    // Mappers
    private final EventMapper eventMapper;
    private final EventUserMapper eventUserMapper;

    // Validators / Generators / Resolvers / Normalizers
    private final ColorValidator colorValidator;
    private final ColorGenerator colorGenerator;
    private final EventPeriodResolver eventPeriodResolver;
    private final EventDateRules eventDateRules;
    private final InputNormalizer inputNormalizer;
    private final PaginationValidator paginationValidator;

    /**
     * Creates a new event within a workspace and assigns the creator to it.
     * This guarantees that every event always has at least one user assigned
     * (the creator).
     */
    @Transactional
    @Override
    public EventCreateResponse createEvent(
            Long workspaceId,
            Long userId,
            EventCreateRequest request) {

        // Loads the workspace and verifies OWNER permissions
        Workspace workspace =
                workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

        eventDateRules.validateDateRange(request.eventDate());

        // Converts the request date (with offset) to an absolute UTC instant
        Instant eventDate = request.eventDate().toInstant();

        // Resolves the event color or generates a default one if missing
        String resolvedColor = ColorUtil.resolveColor(
                request.color(),
                colorValidator,
                colorGenerator
        );

        Event event = Event.builder()
                .title(inputNormalizer.normalize(request.title()))
                .eventDate(eventDate)
                .description(inputNormalizer.normalize(request.description()))
                .scope(request.scope())
                .color(resolvedColor)
                .workspace(workspace)
                .build();

        // Persist the event
        Event savedEvent = eventRepository.save(event);

        // Assign the creator to the event
        EventUser eventUser = EventUser.builder()
                .event(savedEvent)
                .user(userLoader.loadUserOrThrow(userId))
                .build();

        // Persist the event-user association
        eventUserRepository.save(eventUser);

        // Return the created event response
        return eventMapper.toEventCreateResponse(savedEvent);
    }

    @Transactional
    @Override
    public EventResponse updateEvent(
            Long workspaceId,
            Long eventId,
            Long userId,
            EventUpdateRequest request) {

        // Validate workspace exists and user is OWNER
        workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

        // Load event or throw if not found
        Event event = eventLoader.loadOrThrow(workspaceId, eventId);

        // Update optional fields if present
        Optional.ofNullable(request.title())
                .filter(t -> !t.isBlank())
                .map(inputNormalizer::normalize)
                .ifPresent(event::setTitle);

        Optional.ofNullable(request.description())
                .filter(d -> !d.isBlank())
                .map(inputNormalizer::normalize)
                .ifPresent(event::setDescription);

        Optional.ofNullable(request.eventDate())
                .ifPresent(ed -> {
                    eventDateRules.validateDateRange(ed);
                    event.setEventDate(ed.toInstant());
                });

        Optional.ofNullable(request.color())
                .filter(c -> !c.isBlank())
                .ifPresent(c -> event.setColor(
                        ColorUtil.resolveColor(c, colorValidator, colorGenerator)
                ));

        // Persist and return updated event
        return eventMapper.toEventResponse(
                eventRepository.save(event)
        );
    }

    /**
     * <p>Retrieves events within a workspace to which the user is assigned.</p>
     *
     * <p><strong>Behavior:</strong></p>
     * <ul>
     *   <li><code>DAY</code>/<code>WEEK</code>/<code>MONTH</code>: requires a non-null <code>timeZone</code> to calculate the time range.
     *     If a date is provided, the period is calculated relative to that date.
     *     Otherwise, the current date-time is used.</li>
     *   <li><code>ALL</code>: ignores <code>timeZone</code> and returns all events in the workspace for the user.</li>
     * </ul>
     *
     * <p>Throws <code>TimeZoneMissingException</code> if <code>timeZone</code> is missing for
     * <code>DAY</code>/<code>WEEK</code>/<code>MONTH</code>.</p>
     */
    @Transactional(readOnly = true)
    @Override
    public PageResponse<EventListItemResponse> getEvents(
            Long workspaceId,
            Long userId,
            EventPeriod period,
            ZoneId timeZone,
            LocalDate date,
            Pageable pageable
    ) {

        workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId);

        Pageable validatedPageable = paginationValidator.validate(pageable);

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

        Page<Event> page = range
                .map(r -> eventRepository
                        .findEventsByWorkspaceAndUserAndDateRange(
                                workspaceId,
                                userId,
                                r.start(),
                                r.end(),
                                validatedPageable
                        ))
                .orElseGet(() ->
                        eventRepository.findEventsByWorkspaceAndUser(
                                workspaceId,
                                userId,
                                validatedPageable
                        )
                );

        Page<EventListItemResponse> mappedPage =
                page.map(eventMapper::toEventListItemResponse);

        return PageMapper.toPageResponse(mappedPage);
    }

    @Transactional(readOnly = true)
    @Override
    public EventResponse getEvent(
            Long workspaceId,
            Long eventId,
            Long userId) {

        // Validate workspace exists and user has access to it
        workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId);

        // Validate event exists and belongs to the workspace
        eventLoader.loadOrThrow(workspaceId, eventId);

        // Validate user has access to event
        if (!eventUserRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new EventAccessDeniedException();
        }

        // Load and return event details
        return eventMapper.toEventResponse(eventLoader.loadOrThrow(workspaceId, eventId));
    }

    @Transactional
    @Override
    public void deleteEvent(
            Long workspaceId,
            Long eventId,
            Long userId) {

        // Validate workspace exists and user is OWNER
        workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

        // Validate event exists and belongs to the workspace
        eventLoader.loadOrThrow(workspaceId, eventId);

        // Remove all user assignments for the event
        eventUserRepository.deleteByEventId(eventId);

        // Delete the event itself
        eventRepository.deleteById(eventId);
    }

    /** Assigns users to an event, skipping already assigned ones */
    @Transactional
    @Override
    public EventAssignUserResponse assignUsersToEvent(
            Long workspaceId,
            Long eventId,
            Long userId,
            EventAssignUserRequest request) {

        // Validate workspace exists and user is OWNER
        workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

        Event event = eventLoader.loadOrThrow(workspaceId, eventId);

        if (!EventScope.SPECIFIC.equals(event.getScope())) {
            throw new EventNotAssignableException();
        }

        List<Long> userIds = request.userIds();

        // Preload users already assigned to the event
        Set<Long> alreadyAssignedUserIds = eventUserRepository
                .findAllByEventId(eventId)
                .stream()
                .map(eventUser -> eventUser.getUser().getId())
                .collect(Collectors.toSet());

        // Newly assigned users to return
        List<Long> assignedUserIds = new ArrayList<>();

        // EventUser entities to persist
        List<EventUser> newEventUsers = new ArrayList<>();

        for (Long targetUserId : userIds) {

            // Ensure target user belongs to the workspace
            try {
                workspaceAccessService.requireAccessibleWorkspace(workspaceId, targetUserId);
            } catch (WorkspaceAccessDeniedException ex) {
                throw new WorkspaceAccessDeniedException(
                        "Some user does not have access to this workspace: userId=" + targetUserId
                );
            }

            if (!alreadyAssignedUserIds.contains(targetUserId)) {
                newEventUsers.add(
                        EventUser.builder()
                                .event(event)
                                .user(userLoader.loadUserOrThrow(targetUserId))
                                .build()
                );

                // Add to response list
                assignedUserIds.add(targetUserId);
            }
        }

        eventUserRepository.saveAll(newEventUsers);

        // Update hasActiveUsers flag
        updateHasActiveUsers(event);

        // Return only newly assigned users
        return new EventAssignUserResponse(eventId, assignedUserIds);
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<EventAssignedUserResponse> getEventAssignedUsers(
            Long workspaceId,
            Long eventId,
            Long userId,
            Pageable pageable
    ) {

        // Validate workspace exists and user has access
        workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId);

        Pageable validatedPageable = paginationValidator.validate(pageable);

        eventLoader.loadOrThrow(workspaceId, eventId);

        // Validate user has access to event
        if (!eventUserRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new EventAccessDeniedException();
        }

        Page<EventUser> page = eventUserRepository.findAllByEventId(eventId, validatedPageable);

        Page<EventAssignedUserResponse> mappedPage =
                page.map(eventUserMapper::toEventAssignedUserResponse);

        return PageMapper.toPageResponse(mappedPage);
    }

    @Transactional
    @Override
    public void deleteUserFromEvent(
            Long workspaceId,
            Long eventId,
            Long targetUserId,
            Long userId) {

        // Validate workspace exists and user is OWNER
        workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

        Event event = eventLoader.loadOrThrow(workspaceId, eventId);

        if (EventScope.GLOBAL.equals(event.getScope())) {
            throw new EventNotAssignableException();
        }

        // Validate target user is assigned to the event
        if (!eventUserRepository.existsByEventIdAndUserId(eventId, targetUserId)) {
            throw new UserNotAssignedToEventException();
        }

        // Remove user assignment
        eventUserRepository.deleteByEventIdAndUserId(eventId, targetUserId);

        // Update hasActiveUsers flag
        updateHasActiveUsers(event);
    }

    /**
     * Updates the hasActiveUsers flag based on the number of users assigned to the event.
     * True if there is at least one user besides the owner.
     */
    private void updateHasActiveUsers(Event event) {

        long assignedUserCount = eventUserRepository.countByEventId(event.getId());
        event.setHasActiveUsers(assignedUserCount > 1);

        eventRepository.save(event);
    }
}
