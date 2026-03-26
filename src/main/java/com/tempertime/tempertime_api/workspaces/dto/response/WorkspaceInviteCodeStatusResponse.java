package com.tempertime.tempertime_api.workspaces.dto.response;

/**
 * Response indicating whether the workspace invite code is enabled.
 */
public record WorkspaceInviteCodeStatusResponse(

        Boolean inviteEnabled
) {}
