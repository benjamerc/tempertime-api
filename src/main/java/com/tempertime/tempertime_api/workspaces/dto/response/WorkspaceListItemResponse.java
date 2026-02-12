package com.tempertime.tempertime_api.workspaces.dto.response;

import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;

/** Lightweight workspace view used in user workspace listings */
public record WorkspaceListItemResponse(

        Long id,
        String name,
        String color,
        WorkspaceRole userRole,
        Boolean archived
) {}
