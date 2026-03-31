package com.tempertime.tempertime_api.workspaces.repository;

import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing user memberships within workspaces.
 */
@Repository
public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, Long> {

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

    /**
     * Returns all workspaces for a user.
     * Includes filters for role and archived status.
     */
    @Query("""
        SELECT wu
        FROM WorkspaceUser wu
        WHERE wu.user.id = :userId
            AND (:role IS NULL OR wu.role = :role)
            AND (:archived IS NULL OR wu.workspace.archived = :archived)
    """)
    Page<WorkspaceUser> findWorkspacesByUserAndOptionalFilters(
            @Param("userId") Long userId,
            @Param("role") WorkspaceRole role,
            @Param("archived") Boolean archived,
            Pageable pageable
    );

    /**
     * Returns all users in a workspace.
     */
    @Query("""
        SELECT wu.user
        FROM WorkspaceUser wu
        WHERE wu.workspace.id = :workspaceId
    """)
    List<User> findUsersByWorkspaceId(
            @Param("workspaceId") Long workspaceId
    );
}
