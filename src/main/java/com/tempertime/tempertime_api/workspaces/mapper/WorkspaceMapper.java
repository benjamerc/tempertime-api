package com.tempertime.tempertime_api.workspaces.mapper;

import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceResponse;
import com.tempertime.tempertime_api.workspaces.model.Workspace;
import org.mapstruct.Mapper;

/** Maps Workspace domain models to API response DTOs */
@Mapper(componentModel = "spring")
public interface WorkspaceMapper {

    WorkspaceResponse toWorkspaceResponse(Workspace workspace);
}
