package com.tempertime.tempertime_api.events.dto.response;

/** Response containing the users assigned to an event */
public record EventAssignedUserResponse(

        Long userId,
        String firstName,
        String lastName
) {}
