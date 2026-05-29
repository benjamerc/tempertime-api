package com.tempertime.tempertime_api.workspaces.dto.response;

import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response returned after joining a workspace using an invite code.
 */
@Schema(
        description = "Response returned after successfully joining a workspace"
)
public record WorkspaceJoinResponse(

        @Schema(
                description = "Workspace unique identifier",
                example = "1"
        )
        Long workspaceId,

        @Schema(
                description = "User unique identifier",
                example = "1"
        )
        Long userId,

        @Schema(
                description = "Role assigned to the user in the workspace (MEMBER)",
                example = "MEMBER"
        )
        WorkspaceRole role
) {}
