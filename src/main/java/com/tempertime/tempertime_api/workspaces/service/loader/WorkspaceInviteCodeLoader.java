package com.tempertime.tempertime_api.workspaces.service.loader;

import com.tempertime.tempertime_api.common.hash.Hash;
import com.tempertime.tempertime_api.workspaces.exception.InvalidWorkspaceInviteCodeException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceInviteCodeDisabledException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceInviteCodeNotFoundException;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceInviteCode;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceInviteCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Loads WorkspaceCode entities and enforces invite code invariants.
 * Works exclusively with raw invite codes (normalized and hashed internally).
 * Throws domain-specific exceptions if code is invalid, disabled, or not found.
 */
@Service
@RequiredArgsConstructor
public class WorkspaceInviteCodeLoader {

    private final WorkspaceInviteCodeRepository workspaceInviteCodeRepository;

    /**
     * Loads the invite code associated with the given workspace.
     * Throws a domain exception if the workspace has no invite code.
     */
    public WorkspaceInviteCode loadByWorkspaceOrThrow(Workspace workspace) {
        return loadByWorkspaceIdOrThrow(workspace.getId());
    }

    /**
     * Loads the invite code associated with the given workspace ID.
     * Throws a domain exception if not found.
     */
    public WorkspaceInviteCode loadByWorkspaceIdOrThrow(Long workspaceId) {
        return workspaceInviteCodeRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() ->
                        new WorkspaceInviteCodeNotFoundException(
                                "Workspace invite code not found"
                        ));
    }

    /**
     * Loads a workspace invite code by its raw value and ensures it is enabled.
     */
    public WorkspaceInviteCode loadEnabledByCodeOrThrow(String rawInviteCode) {

        WorkspaceInviteCode workspaceInviteCode = loadByCodeOrThrow(rawInviteCode);

        if (!workspaceInviteCode.getInviteEnabled()) {
            throw new WorkspaceInviteCodeDisabledException(
                    "Workspace invite code is disabled"
            );
        }

        return workspaceInviteCode;
    }

    /**
     * Loads a workspace invite code by its raw value.
     * The raw code is normalized and hashed before lookup.
     */
    public WorkspaceInviteCode loadByCodeOrThrow(String rawInviteCode) {

        String normalizedInviteCode = normalize(rawInviteCode);
        String inviteCodeHash = hash(normalizedInviteCode);

        return workspaceInviteCodeRepository.findByInviteCodeHash(inviteCodeHash)
                .orElseThrow(() ->
                        new InvalidWorkspaceInviteCodeException(
                                "Invalid workspace invite code"
                        ));
    }

    /** Normalizes the invite code for consistent handling */
    private String normalize(String rawInviteCode) {
        return rawInviteCode.trim().toUpperCase();
    }

    /** Hashes the normalized invite code using SHA-256 */
    private String hash(String normalizedInviteCode) {
        return Hash.sha256(normalizedInviteCode);
    }
}
