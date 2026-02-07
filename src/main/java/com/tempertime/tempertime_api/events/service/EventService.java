package com.tempertime.tempertime_api.events.service;

import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.request.EventUpdateRequest;
import com.tempertime.tempertime_api.events.dto.response.EventCreateResponse;
import com.tempertime.tempertime_api.events.dto.response.EventListItemResponse;
import com.tempertime.tempertime_api.events.dto.response.EventResponse;

import java.util.List;

public interface EventService {

    EventCreateResponse createEvent(
            Long workspaceId,
            Long userId,
            EventCreateRequest request
    );

    EventResponse updateEvent(
            Long workspaceId,
            Long eventId,
            Long userId,
            EventUpdateRequest request
    );

    /** Retrieves all events from a workspace to which the user is assigned */
    List<EventListItemResponse> getEvents(
            Long workspaceId,
            Long userId
    );

    EventResponse getEvent(
            Long workspaceId,
            Long eventId,
            Long userId
    );

    void deleteEvent(
            Long workspaceId,
            Long eventId,
            Long userId
    );
}
