package com.tempertime.tempertime_api.events.data;

import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.domain.EventScope;
import com.tempertime.tempertime_api.events.domain.EventUser;
import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.response.*;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;

import java.time.Instant;
import java.time.OffsetDateTime;

public class EventTestDataProvider {

    public static final String TITLE = "Test Event";
    public static final String DESCRIPTION = "Test Description";
    public static final String COLOR = "#A3B4C5";

    // Event

    public static Event event(Long id, Workspace workspace) {

        Instant now = Instant.now();

        return Event.builder()
                .id(id)
                .title(TITLE)
                .description(DESCRIPTION)
                .color(COLOR)
                .eventDate(now.plusSeconds(1800))
                .workspace(workspace)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Event specificEvent(Long id, Workspace workspace) {
        Event event = event(id, workspace);
        event.setScope(EventScope.SPECIFIC);
        return event;
    }

    // EventUser

    public static EventUser eventUser(Event event, User user) {
        return EventUser.builder()
                .event(event)
                .user(user)
                .build();
    }

    // Requests

    public static EventCreateRequest eventCreateRequest() {
        return new EventCreateRequest(
                TITLE,
                OffsetDateTime.now().plusHours(1),
                DESCRIPTION,
                EventScope.GLOBAL,
                COLOR
        );
    }

    // Responses

    public static EventCreateResponse eventCreateResponse(Event event) {
        return new EventCreateResponse(
                event.getId(),
                event.getTitle(),
                event.getEventDate(),
                event.getCreatedAt()
        );
    }

    public static EventResponse eventResponse(Event event) {
        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getEventDate(),
                event.getCreatedAt(),
                event.getDescription(),
                event.getScope().name(),
                event.getColor(),
                event.getHasActiveUsers()
        );
    }

    public static EventListItemResponse eventListItemResponse(Event event) {
        return new EventListItemResponse(
                event.getId(),
                event.getTitle(),
                event.getEventDate(),
                event.getColor(),
                event.getHasActiveUsers()
        );
    }

    public static UserEventResponse userEventResponse(Event event) {
        return new UserEventResponse(
                event.getId(),
                event.getTitle(),
                event.getEventDate(),
                event.getColor(),
                event.getWorkspace().getId(),
                event.getWorkspace().getName(),
                event.getWorkspace().getColor()
        );
    }

    public static EventAssignedUserResponse eventAssignedUserResponse(User user) {
        return new EventAssignedUserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName()
        );
    }
}
