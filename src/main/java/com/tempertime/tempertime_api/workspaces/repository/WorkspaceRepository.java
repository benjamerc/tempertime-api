package com.tempertime.tempertime_api.workspaces.repository;

import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Workspace: CRUD operations */
@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
}
