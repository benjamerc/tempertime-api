package com.tempertime.tempertime_api.workspaces.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Represents a workspace invite code.
 */
@Schema(
        description = "Workspace invite code information"
)
public record WorkspaceInviteCodeResponse(

        @Schema(
                description = "Workspace invite code",
                example = "7IHG90J420LQ"
        )
        String inviteCode,

        @Schema(
                description = "Whether the invite code is enabled",
                example = "true"
        )
        Boolean inviteEnabled,

        @Schema(
                description = "Invite code creation timestamp in ISO 8601 (UTC)",
                example = "2026-02-07T10:15:30.339652Z"
        )
        Instant createdAt,

        @Schema(
                description = "Last invite code regeneration timestamp in ISO 8601 (UTC). Null if never regenerated",
                example = "2026-02-10T14:22:11.120531Z",
                nullable = true
        )
        Instant lastRegeneratedAt
) {}
