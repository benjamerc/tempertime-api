package com.tempertime.tempertime_api.workspaces.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request to join a workspace using an invite code.
 */
@Schema(
        description = "Request containing the invite code required to join a workspace"
)
public record WorkspaceJoinRequest(

        @Schema(
                description = "Workspace invite code",
                example = "9FID8P2TTUS9"
        )
        @NotBlank(message = "Invite code is required")
        @Size(min = 12, max = 12, message = "Invite code must be 12 characters")
        String inviteCode
) {}
