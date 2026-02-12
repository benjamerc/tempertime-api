package com.tempertime.tempertime_api.workspaces.service.core;

import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceUpdateRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.*;

import java.util.List;

/** Workspace application service (user-scoped operations) */
public interface WorkspaceService {

    WorkspaceCreateResponse createWorkspace(WorkspaceCreateRequest request, Long userId);

    /** Workspaces the given user is a member of */
    List<WorkspaceListItemResponse> getUserWorkspaces(Long userId);

    /** Workspace details scoped to the given user */
    WorkspaceDetailResponse getWorkspaceById(Long workspaceId, Long userId);

    WorkspaceUpdateResponse updateWorkspace(Long workspaceId, Long userId, WorkspaceUpdateRequest request);

    void archiveWorkspace(Long workspaceId, Long userId);

    void unarchiveWorkspace(Long workspaceId, Long userId);

    void deleteWorkspace(Long workspaceId, Long userId);

    /**
     * Returns workspace invite code metadata (enabled status and creation time).
     * The raw invite code is not exposed.
     */
    WorkspaceInviteCodeResponse getInviteCode(Long workspaceId, Long userId);

    /** Enables the workspace invite code */
    WorkspaceInviteCodeResponse activateInviteCode(Long workspaceId, Long userId);

    /** Disables the workspace invite code */
    WorkspaceInviteCodeResponse deactivateInviteCode(Long workspaceId, Long userId);

    /**
     * Regenerates the workspace invite code.
     * Returns the new invite code along with its status and creation timestamp.
     */
    WorkspaceInviteCodeRegenerateResponse regenerateInviteCode(Long workspaceId, Long userId);

    /** Joins a workspace using a valid invitation code */
    WorkspaceJoinResponse joinWorkspace(String inviteCode, Long userId);

    /** Returns all users of the workspace along with their roles */
    List<WorkspaceUserResponse> getWorkspaceUsers(Long workspaceId, Long userId);

    /** Removes a user from a workspace. Must be performed by the OWNER */
    void removeWorkspaceUser(Long workspaceId, Long targetUserId, Long userId);

    /** Allows a workspace user to leave the workspace. Owners cannot leave themselves. */
    void leaveWorkspace(Long workspaceId, Long userId);
}
