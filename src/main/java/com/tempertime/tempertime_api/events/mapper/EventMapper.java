package com.tempertime.tempertime_api.events.mapper;

import com.tempertime.tempertime_api.events.dto.response.EventCreateResponse;
import com.tempertime.tempertime_api.events.dto.response.EventResponse;
import com.tempertime.tempertime_api.events.model.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventCreateResponse toEventCreateResponse(Event event);

    EventResponse toEventResponse(Event event);
}
