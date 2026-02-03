package com.tempertime.tempertime_api.workspaces.dto.response;

import com.tempertime.tempertime_api.workspaces.model.WorkspaceRole;

import java.time.Instant;

/** Detailed workspace view for a user with membership in the workspace */
public record WorkspaceDetailResponse(

        Long id,
        String name,
        String color,
        WorkspaceRole userRole,
        Instant createdAt,
        Boolean archived
) {}
