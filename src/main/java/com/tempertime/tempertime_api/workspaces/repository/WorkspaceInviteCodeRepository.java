package com.tempertime.tempertime_api.workspaces.repository;

import com.tempertime.tempertime_api.workspaces.domain.WorkspaceInviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceInviteCodeRepository extends JpaRepository<WorkspaceInviteCode, Long> {

    /**
     * Finds the workspace invite code associated with a workspace.
     */
    Optional<WorkspaceInviteCode> findByWorkspaceId(Long workspaceId);

    /**
     * Finds a workspace invite code by its hash.
     */
    Optional<WorkspaceInviteCode> findByInviteCodeHash(String inviteCodeHash);
}
