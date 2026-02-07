package com.tempertime.tempertime_api.events.service.impl;

import com.tempertime.tempertime_api.common.color.ColorGenerator;
import com.tempertime.tempertime_api.common.color.ColorUtil;
import com.tempertime.tempertime_api.common.color.ColorValidator;
import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.request.EventUpdateRequest;
import com.tempertime.tempertime_api.events.dto.response.EventCreateResponse;
import com.tempertime.tempertime_api.events.dto.response.EventListItemResponse;
import com.tempertime.tempertime_api.events.dto.response.EventResponse;
import com.tempertime.tempertime_api.events.exception.EventAccessDeniedException;
import com.tempertime.tempertime_api.events.mapper.EventMapper;
import com.tempertime.tempertime_api.events.model.Event;
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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventUserRepository eventUserRepository;
    private final EventMapper eventMapper;
    private final EventLoader eventLoader;

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
        Event event = eventLoader.loadOrThrow(eventId);

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

    /** Retrieves all events within a workspace to which the user is assigned */
    @Transactional(readOnly = true)
    @Override
    public List<EventListItemResponse> getEvents(Long workspaceId, Long userId) {

        // Validate workspace exists and user has access to it
        workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId);

        // Retrieve events assigned to the user within the workspace
        List<Event> events = eventRepository.findEventsByWorkspaceAndUser(
                workspaceId,
                userId
        );

        return events.stream()
                .map(eventMapper::toEventListItemResponse)
                .toList();
    }

    @Override
    public EventResponse getEvent(Long workspaceId, Long eventId, Long userId) {

        // Validate workspace exists and user has access to it
        workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId);

        // Validate event exists
        eventLoader.loadOrThrow(eventId);

        // Validate user has access to event
        if (!eventUserRepository.existsByEventIdAndUserId(eventId, userId)) {
            throw new EventAccessDeniedException("Event not accessible");
        }

        // Load and return event details
        return eventMapper.toEventResponse(eventLoader.loadOrThrow(eventId));
    }

    @Transactional
    @Override
    public void deleteEvent(Long workspaceId, Long eventId, Long userId) {

        // Validate workspace exists and user is OWNER
        workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

        // Validate event exists
        eventLoader.loadOrThrow(eventId);

        // Remove all user assignments for the event
        eventUserRepository.deleteByEventId(eventId);

        // Delete the event itself
        eventRepository.deleteById(eventId);
    }
}
