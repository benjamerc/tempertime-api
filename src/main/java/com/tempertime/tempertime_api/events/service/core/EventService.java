package com.tempertime.tempertime_api.events.service.core;

import com.tempertime.tempertime_api.events.dto.request.EventAssignUserRequest;
import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.request.EventUpdateRequest;
import com.tempertime.tempertime_api.events.dto.response.*;
import com.tempertime.tempertime_api.events.domain.EventPeriod;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Service for managing workspace events and user assignments.
 */
public interface EventService {

    /**
     * Creates a new event within a workspace.
     */
    EventCreateResponse createEvent(
            Long workspaceId,
            Long userId,
            EventCreateRequest request
    );

    /**
     * Updates an existing event.
     */
    EventResponse updateEvent(
            Long workspaceId,
            Long eventId,
            Long userId,
            EventUpdateRequest request
    );

    /**
     * Finds all events in a workspace assigned to the user.
     */
    List<EventListItemResponse> getEvents(
            Long workspaceId,
            Long userId,
            EventPeriod period,
            ZoneId timeZone,
            LocalDate date
    );

    /**
     * Retrieves a single event by id.
     */
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

    /**
     * Assigns users to an event.
     */
    EventAssignUserResponse assignUsersToEvent(
            Long workspaceId,
            Long eventId,
            Long userId,
            EventAssignUserRequest request
    );

    /**
     * Retrieves users assigned to an event.
     */
    List<EventAssignedUserResponse> getEventAssignedUsers(
            Long workspaceId,
            Long eventId,
            Long userId
    );

    /**
     * Removes a user from an event.
     */
    void deleteUserFromEvent(
            Long workspaceId,
            Long eventId,
            Long targetUserId,
            Long userId
    );
}
