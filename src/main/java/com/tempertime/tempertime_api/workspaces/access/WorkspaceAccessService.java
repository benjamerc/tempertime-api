package com.tempertime.tempertime_api.workspaces.access;

import com.tempertime.tempertime_api.workspaces.model.Workspace;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.service.WorkspaceAuthorizationService;
import com.tempertime.tempertime_api.workspaces.service.WorkspaceLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkspaceAccessService {

    private final WorkspaceLoader workspaceLoader;
    private final WorkspaceAuthorizationService authorizationService;

    /**
     * Ensures the workspace exists and the user belongs to it.
     */
    public void requireAccessibleWorkspace(Long workspaceId, Long userId) {
        workspaceLoader.loadOrThrow(workspaceId);
        authorizationService.requireMembership(workspaceId, userId);
    }

    /**
     * Loads the workspace and ensures the user has OWNER role.
     */
    public Workspace loadWorkspaceWithOwnerAccess(Long workspaceId, Long userId) {
        Workspace workspace = workspaceLoader.loadOrThrow(workspaceId);
        authorizationService.requireRole(workspaceId, userId, WorkspaceRole.OWNER);
        return workspace;
    }
}
