package com.tempertime.tempertime_api.workspaces.repository;

import com.tempertime.tempertime_api.workspaces.domain.WorkspaceUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing user memberships within workspaces.
 */
@Repository
public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, Long> {

    /**
     * Repository for managing user memberships within workspaces.
     */
    List<WorkspaceUser> findAllByUserId(Long userId);

    /**
     * Finds a specific membership by workspace and user IDs.
     */
    Optional<WorkspaceUser> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    /**
     * Checks whether a user exists in a workspace, regardless of role.
     */
    boolean existsByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    /**
     * Returns all users in a workspace.
     */
    List<WorkspaceUser> findByWorkspaceId(Long workspaceId);
}
