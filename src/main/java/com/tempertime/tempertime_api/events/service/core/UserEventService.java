package com.tempertime.tempertime_api.events.service.core;

import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.events.dto.response.UserEventResponse;
import com.tempertime.tempertime_api.events.domain.EventPeriod;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Service for retrieving events assigned to a user.
 */
public interface UserEventService {

    /**
     * Finds events assigned to a user filtered by period.
     */
    PageResponse<UserEventResponse> getUserEvents(
            Long userId,
            EventPeriod period,
            ZoneId timeZone,
            LocalDate date,
            Pageable pageable
    );
}
