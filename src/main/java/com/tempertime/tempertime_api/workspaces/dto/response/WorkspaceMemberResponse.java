package com.tempertime.tempertime_api.workspaces.dto.response;

import com.tempertime.tempertime_api.workspaces.model.WorkspaceRole;

/**
 * Represents a workspace member with basic user information
 * and their role within the workspace.
 */
public record WorkspaceMemberResponse(

        Long id,
        String firstName,
        String lastName,
        WorkspaceRole role
) {}
