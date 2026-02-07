package com.tempertime.tempertime_api.events.service.impl;

import com.tempertime.tempertime_api.common.color.ColorGenerator;
import com.tempertime.tempertime_api.common.color.ColorUtil;
import com.tempertime.tempertime_api.common.color.ColorValidator;
import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.request.EventUpdateRequest;
import com.tempertime.tempertime_api.events.dto.response.EventCreateResponse;
import com.tempertime.tempertime_api.events.dto.response.EventResponse;
import com.tempertime.tempertime_api.events.mapper.EventMapper;
import com.tempertime.tempertime_api.events.model.Event;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.events.service.EventLoader;
import com.tempertime.tempertime_api.events.service.EventService;
import com.tempertime.tempertime_api.workspaces.access.WorkspaceAccessService;
import com.tempertime.tempertime_api.workspaces.model.Workspace;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventLoader eventLoader;

    private final WorkspaceAccessService workspaceAccessService;

    private final ColorValidator colorValidator;
    private final ColorGenerator colorGenerator;

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

        // Persist and return the event
        return eventMapper.toEventCreateResponse(
                eventRepository.save(event)
        );
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
}
