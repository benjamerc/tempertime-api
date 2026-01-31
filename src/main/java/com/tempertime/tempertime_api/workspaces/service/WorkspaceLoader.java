package com.tempertime.tempertime_api.workspaces.service;

import com.tempertime.tempertime_api.workspaces.exception.WorkspaceNotFoundException;
import com.tempertime.tempertime_api.workspaces.model.Workspace;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Loads Workspace entities and throws a domain exception if not found */
@Service
@RequiredArgsConstructor
public class WorkspaceLoader {

    private final WorkspaceRepository workspaceRepository;

    public Workspace loadOrThrow(Long workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() ->
                        new WorkspaceNotFoundException("Workspace not found")
                );
    }
}