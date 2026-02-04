package com.tempertime.tempertime_api.workspaces.service;

import com.tempertime.tempertime_api.common.util.HashUtil;
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
 * Works exclusively with raw invite codes (normalized + hashed internally).
 * Throws domain-specific exceptions if code is invalid, disabled, or not found.
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
     * Loads a workspace invite code by its raw value and ensures it is enabled.
     */
    public WorkspaceCode loadEnabledByCodeOrThrow(String rawInviteCode) {

        WorkspaceCode workspaceCode = loadByCodeOrThrow(rawInviteCode);

        if (!workspaceCode.getInvitationsEnabled()) {
            throw new WorkspaceInviteCodeDisabledException(
                    "Workspace invite code is disabled"
            );
        }

        return workspaceCode;
    }

    /**
     * Loads a workspace invite code by its raw value.
     * The raw code is normalized and hashed before lookup.
     */
    public WorkspaceCode loadByCodeOrThrow(String rawInviteCode) {

        String normalizedCode = normalize(rawInviteCode);
        String codeHash = hash(normalizedCode);

        return workspaceCodeRepository.findByCodeHash(codeHash)
                .orElseThrow(() ->
                        new InvalidWorkspaceInviteCodeException(
                                "Invalid workspace invite code"
                        ));
    }

    /** Normalizes the invite code for consistent handling */
    private String normalize(String code) {
        return code.trim().toUpperCase();
    }

    /** Hashes the normalized invite code using SHA-256 */
    private String hash(String normalizedCode) {
        return HashUtil.hashSHA256(normalizedCode);
    }
}
