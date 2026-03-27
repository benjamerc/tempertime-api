package com.tempertime.tempertime_api.events.service.core;

import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.events.dto.request.EventAssignUserRequest;
import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.request.EventUpdateRequest;
import com.tempertime.tempertime_api.events.dto.response.*;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;

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
    PageResponse<EventListItemResponse> getEvents(
            Long workspaceId,
            Long userId,
            EventPeriod period,
            ZoneId timeZone,
            LocalDate date,
            Pageable pageable
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
    PageResponse<EventAssignedUserResponse> getEventAssignedUsers(
            Long workspaceId,
            Long eventId,
            Long userId,
            Pageable pageable
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
