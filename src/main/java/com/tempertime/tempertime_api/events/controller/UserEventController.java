package com.tempertime.tempertime_api.events.controller;

import com.tempertime.tempertime_api.events.dto.response.UserEventResponse;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.service.core.UserEventService;
import com.tempertime.tempertime_api.security.core.CurrentUserProvider;
import com.tempertime.tempertime_api.security.core.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping("/api/users/me/events")
@RequiredArgsConstructor
public class UserEventController {

    private final UserEventService userEventService;
    private final CurrentUserProvider currentUserProvider;

    /**
     * Returns all events assigned to the authenticated user
     * within the requested period.
     */
    @GetMapping
    public ResponseEntity<List<UserEventResponse>> getUserEvents(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "MONTH") EventPeriod period,
            @RequestParam(required = false) ZoneId timeZone
    ) {
        List<UserEventResponse> events = userEventService.getUserEvents(
                currentUserProvider.getUserId(userDetails),
                period,
                timeZone
        );

        return ResponseEntity.ok(events);
    }
}
