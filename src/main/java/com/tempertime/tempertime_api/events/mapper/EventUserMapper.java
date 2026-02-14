package com.tempertime.tempertime_api.events.mapper;

import com.tempertime.tempertime_api.events.dto.response.EventAssignedUserResponse;
import com.tempertime.tempertime_api.events.domain.EventUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps EventUser entities to assigned user response DTOs.
 */
@Mapper(componentModel = "spring")
public interface EventUserMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.firstName", target = "firstName")
    @Mapping(source = "user.lastName", target = "lastName")
    EventAssignedUserResponse toEventAssignedUserResponse(EventUser eventUser);
}
