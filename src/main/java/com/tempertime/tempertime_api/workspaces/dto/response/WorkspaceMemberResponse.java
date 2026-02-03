package com.tempertime.tempertime_api.workspaces.dto.response;

import com.tempertime.tempertime_api.workspaces.model.WorkspaceRole;

/** Represents a member of a workspace with basic user information and role */
public record WorkspaceMemberResponse(

        Long id,
        String firstName,
        String lastName,
        WorkspaceRole role
) {}
