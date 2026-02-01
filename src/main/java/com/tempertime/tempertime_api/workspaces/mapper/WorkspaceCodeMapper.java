package com.tempertime.tempertime_api.workspaces.mapper;

import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceCodeResponse;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceCode;
import org.mapstruct.Mapper;

/** Maps WorkspaceCode entities to API response DTOs */
@Mapper(componentModel = "spring")
public interface WorkspaceCodeMapper {

    WorkspaceCodeResponse toWorkspaceCodeResponse(WorkspaceCode workspaceCode);

}
