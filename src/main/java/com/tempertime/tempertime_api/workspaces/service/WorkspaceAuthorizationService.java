package com.tempertime.tempertime_api.workspaces.service;

import com.tempertime.tempertime_api.workspaces.exception.WorkspaceAccessDeniedException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceRoleDeniedException;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceUser;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Enforces workspace access and role-based authorization rules */
@Service
@RequiredArgsConstructor
public class WorkspaceAuthorizationService {

    private final WorkspaceUserRepository workspaceUserRepository;

    /** Ensures the user is a member of the workspace */
    public void requireMember(Long workspaceId, Long userId) {
        if (!workspaceUserRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new WorkspaceAccessDeniedException("Workspace not accessible");
        }
    }

    /** Ensures the user is a member of the workspace and returns the membership */
    public WorkspaceUser requireMemberAndGet(Long workspaceId, Long userId) {
        return workspaceUserRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() ->
                        new WorkspaceAccessDeniedException("Workspace not accessible"));
    }

    /** Ensures the user has OWNER role in the workspace */
    public void requireOwner(Long workspaceId, Long userId) {
        if (!workspaceUserRepository
                .existsByWorkspaceIdAndUserIdAndRole(workspaceId, userId, WorkspaceRole.OWNER)) {
            throw new WorkspaceRoleDeniedException("User does not have sufficient permissions");
        }
    }
}
