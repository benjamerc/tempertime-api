package com.tempertime.tempertime_api.workspaces.mapper;

import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceInviteCodeRegenerateResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceInviteCodeResponse;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceInviteCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Maps WorkspaceInviteCode entities to API response DTOs.
 */
@Mapper(componentModel = "spring")
public interface WorkspaceInviteCodeMapper {

    WorkspaceInviteCodeResponse toWorkspaceInviteCodeResponse(WorkspaceInviteCode workspaceInviteCode);

    /**
     * Maps a workspace invite code regeneration result.
     * The inviteCode parameter represents the raw value and is not persisted.
     */
    @Mapping(target = "inviteCode", source = "inviteCode")
    @Mapping(target = "inviteEnabled", source = "workspaceInviteCode.inviteEnabled")
    @Mapping(target = "createdAt", source = "workspaceInviteCode.createdAt")
    WorkspaceInviteCodeRegenerateResponse toWorkspaceInviteCodeRegenerateResponse(
            WorkspaceInviteCode workspaceInviteCode,
            String inviteCode
    );
}
