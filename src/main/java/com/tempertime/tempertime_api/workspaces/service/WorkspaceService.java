package com.tempertime.tempertime_api.workspaces.service;

import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceUpdateRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.*;

import java.util.List;

/** Workspace application service (user-scoped operations) */
public interface WorkspaceService {

    WorkspaceResponse createWorkspace(WorkspaceCreateRequest request, Long userId);

    /** Workspaces the given user is a member of */
    List<WorkspaceListItemResponse> getUserWorkspaces(Long userId);

    /** Workspace details scoped to the given user */
    WorkspaceDetailResponse getWorkspaceById(Long workspaceId, Long userId);

    WorkspaceResponse updateWorkspace(Long workspaceId, Long userId, WorkspaceUpdateRequest request);

    void archiveWorkspace(Long workspaceId, Long userId);

    void unarchiveWorkspace(Long workspaceId, Long userId);

    void deleteWorkspace(Long workspaceId, Long userId);

    /** Returns the current invitation code for a workspace */
    WorkspaceCodeResponse getInviteCode(Long workspaceId, Long userId);

    WorkspaceCodeResponse enableInviteCode(Long workspaceId, Long userId);

    WorkspaceCodeResponse disableInviteCode(Long workspaceId, Long userId);

    WorkspaceCodeResponse regenerateInviteCode(Long workspaceId, Long userId);

    /** Joins a workspace using a valid invitation code */
    WorkspaceJoinResponse joinWorkspace(String inviteCode, Long userId);
}
