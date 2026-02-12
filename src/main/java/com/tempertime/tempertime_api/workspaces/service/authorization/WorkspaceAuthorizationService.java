package com.tempertime.tempertime_api.workspaces.service.authorization;

import com.tempertime.tempertime_api.workspaces.exception.WorkspaceAccessDeniedException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceRoleDeniedException;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceUser;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Enforces workspace access and role-based authorization rules */
@Service
@RequiredArgsConstructor
public class WorkspaceAuthorizationService {

    private final WorkspaceUserRepository workspaceUserRepository;

    /** Ensures the user has a membership in the workspace (any role) */
    public WorkspaceUser requireMembership(Long workspaceId, Long userId) {
        return workspaceUserRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() ->
                        new WorkspaceAccessDeniedException("Workspace not accessible"));
    }

    /** Ensures the user has the required role within the workspace */
    public void requireRole(
            Long workspaceId,
            Long userId,
            WorkspaceRole requiredRole
    ) {
        WorkspaceUser membership = requireMembership(workspaceId, userId);

        if (membership.getRole() != requiredRole) {
            throw new WorkspaceRoleDeniedException(
                    "User does not have sufficient permissions"
            );
        }
    }
}
