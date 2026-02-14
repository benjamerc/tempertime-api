package com.tempertime.tempertime_api.events.service.core;

import com.tempertime.tempertime_api.events.dto.response.UserEventResponse;
import com.tempertime.tempertime_api.events.domain.EventPeriod;

import java.time.ZoneId;
import java.util.List;

/**
 * Service for retrieving events assigned to a user.
 */
public interface UserEventService {

    /**
     * Finds events assigned to a user filtered by period.
     */
    List<UserEventResponse> getUserEvents(
            Long userId,
            EventPeriod period,
            ZoneId timeZone
    );
}
