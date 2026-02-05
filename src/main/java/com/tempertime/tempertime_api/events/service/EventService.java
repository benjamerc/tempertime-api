package com.tempertime.tempertime_api.events.service;

import com.tempertime.tempertime_api.events.dto.request.EventCreateRequest;
import com.tempertime.tempertime_api.events.dto.response.EventCreateResponse;

public interface EventService {

    EventCreateResponse createEvent(
            Long workspaceId,
            Long userId,
            EventCreateRequest request
    );
}
