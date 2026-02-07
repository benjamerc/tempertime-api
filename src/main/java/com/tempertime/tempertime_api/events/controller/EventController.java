package com.tempertime.tempertime_api.events.controller;

import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.request.EventUpdateRequest;
import com.tempertime.tempertime_api.events.dto.response.EventCreateResponse;
import com.tempertime.tempertime_api.events.dto.response.EventResponse;
import com.tempertime.tempertime_api.events.service.EventService;
import com.tempertime.tempertime_api.security.core.CurrentUserProvider;
import com.tempertime.tempertime_api.security.core.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final CurrentUserProvider currentUserProvider;

    @PostMapping
    public ResponseEntity<EventCreateResponse> createEvent(
            @PathVariable Long workspaceId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EventCreateRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventService.createEvent(
                        workspaceId,
                        currentUserProvider.getUserId(userDetails),
                        request
                ));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long workspaceId,
            @PathVariable Long eventId,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EventUpdateRequest request) {

        EventResponse updatedEvent = eventService.updateEvent(
                workspaceId,
                eventId,
                currentUserProvider.getUserId(userDetails),
                request
        );

        return ResponseEntity.ok(updatedEvent);
    }
}
