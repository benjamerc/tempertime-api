package com.tempertime.tempertime_api.workspaces.dto.response;

import com.tempertime.tempertime_api.workspaces.model.WorkspaceRole;

/**
 * Represents a workspace user with basic user information
 * and their role within the workspace.
 */
public record WorkspaceUserResponse(

        Long id,
        String firstName,
        String lastName,
        WorkspaceRole role
) {}
