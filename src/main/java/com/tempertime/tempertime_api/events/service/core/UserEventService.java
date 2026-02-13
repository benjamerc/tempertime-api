package com.tempertime.tempertime_api.events.service.core;

import com.tempertime.tempertime_api.events.dto.response.UserEventResponse;
import com.tempertime.tempertime_api.events.domain.EventPeriod;

import java.time.ZoneId;
import java.util.List;

/** Service for retrieving events assigned to a specific user */
public interface UserEventService {

    /** Returns events assigned to the given user, filtered by time period */
    List<UserEventResponse> getUserEvents(
            Long userId,
            EventPeriod period,
            ZoneId timeZone
    );
}
