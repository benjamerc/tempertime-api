package com.tempertime.tempertime_api.events.service.impl;

import com.tempertime.tempertime_api.common.color.ColorGenerator;
import com.tempertime.tempertime_api.common.color.ColorUtil;
import com.tempertime.tempertime_api.common.color.ColorValidator;
import com.tempertime.tempertime_api.events.deserializer.EventOffsetDateTimeDeserializer;
import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.response.EventCreateResponse;
import com.tempertime.tempertime_api.events.mapper.EventMapper;
import com.tempertime.tempertime_api.events.model.Event;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.events.service.EventService;
import com.tempertime.tempertime_api.workspaces.access.WorkspaceAccessService;
import com.tempertime.tempertime_api.workspaces.model.Workspace;
import com.tempertime.tempertime_api.workspaces.service.WorkspaceLoader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;

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

        return eventMapper.toEventCreateResponse(
                eventRepository.save(event)
        );
    }
}
