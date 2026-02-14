package com.tempertime.tempertime_api.workspaces.dto.response;

import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;

import java.time.Instant;

/**
 * Detailed workspace view for a user who is part of the workspace.
 */
public record WorkspaceDetailResponse(

        Long id,
        String name,
        String color,
        WorkspaceRole userRole,
        Instant createdAt,
        Boolean archived
) {}
