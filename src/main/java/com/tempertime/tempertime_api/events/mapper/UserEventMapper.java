package com.tempertime.tempertime_api.events.mapper;

import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.dto.response.UserEventResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps Event entities to UserEvent DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserEventMapper {

    @Mapping(target = "workspaceId", source = "workspace.id")
    @Mapping(target = "workspaceName", source = "workspace.name")
    @Mapping(target = "workspaceColor", source = "workspace.color")
    UserEventResponse toUserEventResponse(Event event);
}