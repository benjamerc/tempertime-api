package com.tempertime.tempertime_api.events.service.impl;

import com.tempertime.tempertime_api.common.color.ColorGenerator;
import com.tempertime.tempertime_api.common.color.ColorUtil;
import com.tempertime.tempertime_api.common.color.ColorValidator;
import com.tempertime.tempertime_api.events.dto.internal.TimeRange;
import com.tempertime.tempertime_api.events.dto.request.EventAssignUserRequest;
import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.request.EventUpdateRequest;
import com.tempertime.tempertime_api.events.dto.response.*;
import com.tempertime.tempertime_api.events.exception.EventAccessDeniedException;
import com.tempertime.tempertime_api.events.exception.EventNotAssignableException;
import com.tempertime.tempertime_api.events.exception.UserNotAssignedToEventException;
import com.tempertime.tempertime_api.events.filter.EventPeriodResolver;
import com.tempertime.tempertime_api.events.mapper.EventMapper;
import com.tempertime.tempertime_api.events.mapper.EventUserMapper;
import com.tempertime.tempertime_api.events.model.Event;
import com.tempertime.tempertime_api.events.model.EventPeriod;
import com.tempertime.tempertime_api.events.model.EventScope;
import com.tempertime.tempertime_api.events.model.EventUser;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.events.repository.EventUserRepository;
import com.tempertime.tempertime_api.events.service.EventLoader;
import com.tempertime.tempertime_api.events.service.EventService;
import com.tempertime.tempertime_api.users.service.UserLoader;
import com.tempertime.tempertime_api.workspaces.access.WorkspaceAccessService;
import com.tempertime.tempertime_api.workspaces.model.Workspace;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventUserRepository eventUserRepository;
    private final EventMapper eventMapper;
    private final EventLoader eventLoader;
    private final EventUserMapper eventUserMapper;
    private final EventPeriodResolver eventPeriodResolver;

    private final WorkspaceAccessService workspaceAccessService;

    private final UserLoader userLoader;

    private final ColorValidator colorValidator;
    private final ColorGenerator colorGenerator;

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

        // Converts the request date (with offset) to an absolute UTC instant
        Instant eventDate = request.eventDate().toInstant();

        // Resolves the event color or generates a default one if missing
        String resolvedColor = ColorUtil.resolveColor(
                request.color(),
                colorValidator,
                colorGenerator
        );

        Event event = Event.builder()
                .title(request.title())
                .eventDate(eventDate)
                .description(request.description())
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
                .ifPresent(event::setTitle);

        Optional.ofNullable(request.description())
                .filter(d -> !d.isBlank())
                .ifPresent(event::setDescription);

        Optional.ofNullable(request.eventDate())
                .ifPresent(ed -> event.setEventDate(ed.toInstant()));

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

    /** Retrieves events within a workspace to which the user is assigned,
     * optionally filtered by EventPeriod.
     */
    @Transactional(readOnly = true)
    @Override
    public List<EventListItemResponse> getEvents(
            Long workspaceId,
            Long userId,
            EventPeriod period,
            ZoneId zone) {

        // Validate workspace exists and user has access to it
        workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId);

        Optional<TimeRange> range =
                eventPeriodResolver.resolve(period, zone);

        List<Event> events = range
                .map(r -> eventRepository
                        .findEventsByWorkspaceAndUserAndDateRange(
                                workspaceId,
                                userId,
                                r.start(),
                                r.end()
                        ))
                .orElseGet(() ->
                        eventRepository.findEventsByWorkspaceAndUser(
                                workspaceId,
                                userId
                        )
                );

        return events.stream()
                .map(eventMapper::toEventListItemResponse)
                .toList();
    }

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
            throw new EventAccessDeniedException("Event not accessible");
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
            throw new EventNotAssignableException("Event is not assignable");
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
            workspaceAccessService.requireAccessibleWorkspace(workspaceId, targetUserId);

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

        // Return only newly assigned users
        return new EventAssignUserResponse(eventId, assignedUserIds);
    }

    @Override
    public List<EventAssignedUserResponse> getEventAssignedUsers(
            Long workspaceId,
            Long eventId,
            Long userId) {

        // Validate workspace exists and user has access
        workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId);

        eventLoader.loadOrThrow(workspaceId, eventId);

        // Validate user has access to event
        if (!eventUserRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new EventAccessDeniedException("Event not accessible");
        }

        return eventUserRepository.findAllByEventId(eventId)
                .stream()
                .map(eventUserMapper::toEventAssignedUserResponse)
                .toList();
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
            throw new EventNotAssignableException("Event is not assignable");
        }

        // Validate target user is assigned to the event
        if (!eventUserRepository.existsByEventIdAndUserId(eventId, targetUserId)) {
            throw new UserNotAssignedToEventException("User is not assigned to the event");
        }

        // Remove user assignment
        eventUserRepository.deleteByEventIdAndUserId(eventId, targetUserId);
    }
}
