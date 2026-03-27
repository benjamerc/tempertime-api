package com.tempertime.tempertime_api.events.controller;

import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.events.dto.response.UserEventResponse;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import com.tempertime.tempertime_api.events.service.core.UserEventService;
import com.tempertime.tempertime_api.security.core.CurrentUserProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;

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
    public ResponseEntity<PageResponse<UserEventResponse>> getUserEvents(
            @RequestParam(defaultValue = "MONTH") EventPeriod period,
            @RequestParam(required = false) ZoneId timeZone,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @PageableDefault(
                    page = 0,
                    size = 20,
                    sort = "eventDate",
                    direction = Sort.Direction.ASC
            )
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                userEventService.getUserEvents(
                        currentUserProvider.getUserId(),
                        period,
                        timeZone,
                        date,
                        pageable
                )
        );
    }
}
