package com.tempertime.tempertime_api.events.mapper;

import com.tempertime.tempertime_api.events.dto.response.EventCreateResponse;
import com.tempertime.tempertime_api.events.dto.response.EventListItemResponse;
import com.tempertime.tempertime_api.events.dto.response.EventResponse;
import com.tempertime.tempertime_api.events.domain.Event;
import org.mapstruct.Mapper;

/**
 * Maps Event entities to response DTOs.
 */
@Mapper(componentModel = "spring")
public interface EventMapper {

    EventCreateResponse toEventCreateResponse(Event event);

    EventResponse toEventResponse(Event event);

    EventListItemResponse toEventListItemResponse(Event event);
}
