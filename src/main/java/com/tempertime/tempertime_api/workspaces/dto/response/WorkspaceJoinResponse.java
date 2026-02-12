package com.tempertime.tempertime_api.workspaces.dto.response;

import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;

/** Response returned after joining a workspace using an invite code */
public record WorkspaceJoinResponse(

        Long workspaceId,
        Long userId,
        WorkspaceRole role
) {}
