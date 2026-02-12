package com.tempertime.tempertime_api.workspaces.service.authorization;

import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceUser;
import com.tempertime.tempertime_api.workspaces.service.loader.WorkspaceLoader;
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
    public WorkspaceUser requireAccessibleWorkspace(Long workspaceId, Long userId) {
        workspaceLoader.loadOrThrow(workspaceId);
        return authorizationService.requireMembership(workspaceId, userId);
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
