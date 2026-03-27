package com.tempertime.tempertime_api.workspaces.repository;

import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing user memberships within workspaces.
 */
@Repository
public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, Long> {

    /**
     * Repository for managing user memberships within workspaces.
     */
    Page<WorkspaceUser> findAllByUserId(
            Long userId,
            Pageable pageable
    );

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
    Page<WorkspaceUser> findByWorkspaceId(
            Long workspaceId,
            Pageable pageable
    );

    /**
     * Counts the number of users in a workspace.
     */
    long countByWorkspaceId(Long workspaceId);

    /**
     * Checks if the user has OWNER role in any workspace.
     */
    boolean existsByUserIdAndRole(Long userId, WorkspaceRole role);
}
