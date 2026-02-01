package com.tempertime.tempertime_api.workspaces.repository;

import com.tempertime.tempertime_api.workspaces.model.WorkspaceCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkspaceCodeRepository extends JpaRepository<WorkspaceCode, Long> {

    /** Finds the invite code for a given workspace */
    Optional<WorkspaceCode> findByWorkspaceId(Long workspaceId);

    /** Finds a workspace code by the invite code string */
    Optional<WorkspaceCode> findByCode(String code);
}
