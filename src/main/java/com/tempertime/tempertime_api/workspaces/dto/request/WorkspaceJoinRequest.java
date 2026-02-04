package com.tempertime.tempertime_api.workspaces.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request to join a workspace using an invitation code */
public record WorkspaceJoinRequest(

        @NotBlank(message = "Code is required")
        @Size(max = 12, message = "Code must be at most 12 characters")
        String inviteCode
) {}
