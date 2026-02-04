package com.tempertime.tempertime_api.workspaces.dto.response;

/** Workspace representation returned after create operation */
public record WorkspaceCreateResponse(

        Long id,
        String name,
        String color,
        String inviteCode
) {}
