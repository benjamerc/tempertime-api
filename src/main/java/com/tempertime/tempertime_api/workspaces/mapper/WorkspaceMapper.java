package com.tempertime.tempertime_api.workspaces.mapper;

import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceCreateResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceUpdateResponse;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps Workspace domain models to API response DTOs.
 */
@Mapper(componentModel = "spring")
public interface WorkspaceMapper {

    WorkspaceUpdateResponse toWorkspaceUpdateResponse(Workspace workspace);

    @Mapping(target = "inviteCode", source = "inviteCode")
    WorkspaceCreateResponse toWorkspaceCreateResponse(
            Workspace workspace,
            String inviteCode
    );
}
