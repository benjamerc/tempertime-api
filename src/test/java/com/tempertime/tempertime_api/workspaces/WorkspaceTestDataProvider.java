package com.tempertime.tempertime_api.workspaces;

import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceInviteCode;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceUser;
import com.tempertime.tempertime_api.workspaces.dto.response.*;

import java.time.Instant;

public class WorkspaceTestDataProvider {

    public static final String NAME = "Test Workspace";
    public static final String COLOR = "#A3B4C5";
    public static final String INVITE_CODE = "TESTCODE1234";
    public static final String INVITE_CODE_ENCRYPTED = "encryptedTestCode";
    public static final String INVITE_CODE_HASH = "hashedTestCode";

    // Workspace

    public static Workspace workspace(Long id) {

        Instant now = Instant.now();

        return Workspace.builder()
                .id(id)
                .name(NAME)
                .color(COLOR)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static Workspace archivedWorkspace(Long id) {
        Workspace workspace = workspace(id);
        workspace.setArchived(true);
        return workspace;
    }

    // WorkspaceInviteCode

    public static WorkspaceInviteCode inviteCode(Workspace workspace) {

        Instant now = Instant.now();

        return WorkspaceInviteCode.builder()
                .workspace(workspace)
                .inviteCodeEncrypted(INVITE_CODE_ENCRYPTED)
                .inviteCodeHash(INVITE_CODE_HASH)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static WorkspaceInviteCode inviteCode(Workspace workspace, String encrypted, String hash) {

        Instant now = Instant.now();

        return WorkspaceInviteCode.builder()
                .workspace(workspace)
                .inviteCodeEncrypted(encrypted)
                .inviteCodeHash(hash)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static WorkspaceInviteCode disabledInviteCode(Workspace workspace) {
        WorkspaceInviteCode code = inviteCode(workspace);
        code.setInviteEnabled(false);
        return code;
    }

    // WorkspaceUser

    public static WorkspaceUser workspaceUser(Workspace workspace, User user, WorkspaceRole role) {

        Instant now = Instant.now();

        return WorkspaceUser.builder()
                .workspace(workspace)
                .user(user)
                .role(role)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    public static WorkspaceUser ownerWorkspaceUser(Workspace workspace, User user) {
        return workspaceUser(workspace, user, WorkspaceRole.OWNER);
    }

    public static WorkspaceUser memberWorkspaceUser(Workspace workspace, User user) {
        return workspaceUser(workspace, user, WorkspaceRole.MEMBER);
    }

    // Responses

    public static WorkspaceCreateResponse workspaceCreateResponse(Workspace workspace) {
        return new WorkspaceCreateResponse(
                workspace.getId(),
                workspace.getName(),
                INVITE_CODE
        );
    }

    public static WorkspaceDetailResponse workspaceDetailResponse(Workspace workspace, WorkspaceRole role) {
        return new WorkspaceDetailResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getColor(),
                role,
                workspace.getCreatedAt(),
                workspace.getArchived()
        );
    }

    public static WorkspaceListItemResponse workspaceListItemResponse(Workspace workspace, WorkspaceRole role) {
        return new WorkspaceListItemResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getColor(),
                role,
                workspace.getArchived()
        );
    }

    public static WorkspaceUpdateResponse workspaceUpdateResponse(Workspace workspace) {
        return new WorkspaceUpdateResponse(
                workspace.getId(),
                workspace.getName(),
                workspace.getColor(),
                workspace.getCreatedAt(),
                workspace.getArchived()
        );
    }

    public static WorkspaceJoinResponse workspaceJoinResponse(WorkspaceUser workspaceUser) {
        return new WorkspaceJoinResponse(
                workspaceUser.getWorkspace().getId(),
                workspaceUser.getUser().getId(),
                workspaceUser.getRole()
        );
    }

    public static WorkspaceInviteCodeResponse workspaceInviteCodeResponse(WorkspaceInviteCode inviteCode) {
        return new WorkspaceInviteCodeResponse(
                INVITE_CODE,
                inviteCode.getInviteEnabled(),
                inviteCode.getCreatedAt(),
                inviteCode.getLastRegeneratedAt()
        );
    }
}
