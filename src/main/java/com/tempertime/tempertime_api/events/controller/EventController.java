package com.tempertime.tempertime_api.events.controller;

import com.tempertime.tempertime_api.events.dto.request.EventAssignUserRequest;
import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.request.EventUpdateRequest;
import com.tempertime.tempertime_api.events.dto.response.*;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.service.core.EventService;
import com.tempertime.tempertime_api.security.core.CurrentUserProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EventCreateResponse> createEvent(
            @PathVariable Long workspaceId,
            @Valid @RequestBody EventCreateRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        eventService.createEvent(
                                workspaceId,
                                currentUserProvider.getUserId(),
                                request
                        )
                );
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EventListItemResponse>> getEvents(
            @PathVariable Long workspaceId,
            @RequestParam(defaultValue = "MONTH") EventPeriod period,
            @RequestParam(required = false) ZoneId timeZone,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {

        return ResponseEntity.ok(
                eventService.getEvents(
                        workspaceId,
                        currentUserProvider.getUserId(),
                        period,
                        timeZone,
                        date
                )
        );
    }

    @GetMapping("/{eventId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EventResponse> getEvent(
            @PathVariable Long workspaceId,
            @PathVariable Long eventId
    ) {

        return ResponseEntity.ok(
                eventService.getEvent(
                        workspaceId,
                        eventId,
                        currentUserProvider.getUserId()
                )
        );
    }

    @PatchMapping("/{eventId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long workspaceId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventUpdateRequest request
    ) {

        return ResponseEntity.ok(
                eventService.updateEvent(
                        workspaceId,
                        eventId,
                        currentUserProvider.getUserId(),
                        request
                )
        );
    }

    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long workspaceId,
            @PathVariable Long eventId
    ) {

        eventService.deleteEvent(
                workspaceId,
                eventId,
                currentUserProvider.getUserId()
        );

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{eventId}/users")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<EventAssignUserResponse> assignUsersToEvent(
            @PathVariable Long workspaceId,
            @PathVariable Long eventId,
            @Valid @RequestBody EventAssignUserRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        eventService.assignUsersToEvent(
                                workspaceId,
                                eventId,
                                currentUserProvider.getUserId(),
                                request
                        )
                );
    }

    @GetMapping("/{eventId}/users")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<EventAssignedUserResponse>> getEventAssignedUsers(
            @PathVariable Long workspaceId,
            @PathVariable Long eventId
    ) {

        return ResponseEntity.ok(
                eventService.getEventAssignedUsers(
                        workspaceId,
                        eventId,
                        currentUserProvider.getUserId()
                )
        );
    }

    @DeleteMapping("/{eventId}/users/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteUserFromEvent(
            @PathVariable Long workspaceId,
            @PathVariable Long eventId,
            @PathVariable("userId") Long targetUserId
    ) {

        eventService.deleteUserFromEvent(
                workspaceId,
                eventId,
                targetUserId,
                currentUserProvider.getUserId()
        );

        return ResponseEntity.noContent().build();
    }
}
