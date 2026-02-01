package com.tempertime.tempertime_api.workspaces.dto.response;

import com.tempertime.tempertime_api.workspaces.model.WorkspaceRole;

/** Response after joining a workspace with an invitation code */
public record WorkspaceJoinResponse(

        Long workspaceId,
        Long userId,
        WorkspaceRole role
) {}
