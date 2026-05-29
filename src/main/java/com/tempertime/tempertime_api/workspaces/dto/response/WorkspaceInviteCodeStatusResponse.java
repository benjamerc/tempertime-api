package com.tempertime.tempertime_api.workspaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response indicating whether the workspace invite code is enabled.
 */
@Schema(
        description = "Workspace invite code enabled status"
)
public record WorkspaceInviteCodeStatusResponse(

        @Schema(
                description = "Whether the workspace invite code is enabled"
        )
        Boolean inviteEnabled
) {}
