package com.tempertime.tempertime_api.workspaces.mapper;

import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceCodeRegenerateResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceCodeResponse;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/** Maps WorkspaceCode entities to API response DTOs */
@Mapper(componentModel = "spring")
public interface WorkspaceCodeMapper {

    WorkspaceCodeResponse toWorkspaceCodeResponse(WorkspaceCode workspaceCode);

    /**
     * Maps a workspace invite code regeneration result.
     * The inviteCode parameter represents the raw value and is not persisted.
     */
    @Mapping(target = "inviteCode", source = "inviteCode")
    @Mapping(target = "invitationsEnabled", source = "workspaceCode.invitationsEnabled")
    @Mapping(target = "createdAt", source = "workspaceCode.createdAt")
    WorkspaceCodeRegenerateResponse toWorkspaceCodeRegenerateResponse(
            WorkspaceCode workspaceCode,
            String inviteCode
    );
}
