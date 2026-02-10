package com.tempertime.tempertime_api.events.dto.response;

import java.util.List;

/** Response returned after assigning users to an event */
public record EventAssignUserResponse(

        Long eventId,
        List<Long> userIds
) {}
