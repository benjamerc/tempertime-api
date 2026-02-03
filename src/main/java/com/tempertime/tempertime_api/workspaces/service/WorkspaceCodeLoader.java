package com.tempertime.tempertime_api.workspaces.service;

import com.tempertime.tempertime_api.workspaces.exception.InvalidWorkspaceInviteCodeException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceInviteCodeDisabledException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceInviteCodeNotFoundException;
import com.tempertime.tempertime_api.workspaces.model.Workspace;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceCode;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Loads WorkspaceCode entities and enforces invite code invariants.
 * Throws domain-specific exceptions when the code is not found or invalid.
 */
@Service
@RequiredArgsConstructor
public class WorkspaceCodeLoader {

    private final WorkspaceCodeRepository workspaceCodeRepository;

    /**
     * Loads the invite code associated with the given workspace.
     * Throws a domain exception if the workspace has no invite code.
     */
    public WorkspaceCode loadByWorkspaceOrThrow(Workspace workspace) {
        return loadByWorkspaceIdOrThrow(workspace.getId());
    }

    /**
     * Loads the invite code associated with the given workspace ID.
     * Throws a domain exception if not found.
     */
    public WorkspaceCode loadByWorkspaceIdOrThrow(Long workspaceId) {
        return workspaceCodeRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() ->
                        new WorkspaceInviteCodeNotFoundException(
                                "Workspace invite code not found"
                        ));
    }

    /**
     * Loads a workspace invite code by its value.
     * Throws a domain exception if the code does not exist.
     */
    public WorkspaceCode loadByCodeOrThrow(String code) {
        return workspaceCodeRepository.findByCode(code)
                .orElseThrow(() ->
                        new InvalidWorkspaceInviteCodeException(
                                "Invalid workspace invite code"
                        ));
    }

    /**
     * Loads a workspace invite code by its value and ensures it is enabled.
     * Throws a domain exception if the code is invalid or disabled.
     */
    public WorkspaceCode loadEnabledByCodeOrThrow(String code) {

        WorkspaceCode workspaceCode = loadByCodeOrThrow(code);

        if (!workspaceCode.getEnabled()) {
            throw new WorkspaceInviteCodeDisabledException(
                    "Workspace invite code is disabled"
            );
        }

        return workspaceCode;
    }
}
