package com.tempertime.tempertime_api.workspaces.service.impl;

import com.tempertime.tempertime_api.users.model.User;
import com.tempertime.tempertime_api.users.service.UserLoader;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceUpdateRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.*;
import com.tempertime.tempertime_api.workspaces.exception.*;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceCodeMapper;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceMapper;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceUserMapper;
import com.tempertime.tempertime_api.workspaces.model.Workspace;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceCode;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceUser;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceCodeRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceUserRepository;
import com.tempertime.tempertime_api.workspaces.service.*;
import com.tempertime.tempertime_api.workspaces.support.WorkspaceColorGenerator;
import com.tempertime.tempertime_api.workspaces.support.WorkspaceColorUtil;
import com.tempertime.tempertime_api.workspaces.support.WorkspaceColorValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final WorkspaceMapper workspaceMapper;
    private final WorkspaceUserMapper workspaceUserMapper;
    private final WorkspaceColorValidator workspaceColorValidator;
    private final WorkspaceColorGenerator workspaceColorGenerator;
    private final UserLoader userLoader;
    private final WorkspaceLoader workspaceLoader;
    private final WorkspaceAuthorizationService workspaceAuthorizationService;
    private final WorkspaceCodeGenerator  workspaceCodeGenerator;
    private final WorkspaceCodeRepository  workspaceCodeRepository;
    private final WorkspaceCodeMapper workspaceCodeMapper;
    private final WorkspaceCodeLoader workspaceCodeLoader;

    /**
     * Creates a new workspace and assigns the creator as OWNER.
     * If no color is provided, one is automatically resolved.
     */
    @Transactional
    @Override
    public WorkspaceResponse createWorkspace(WorkspaceCreateRequest request, Long userId) {

        User user = userLoader.loadUserOrThrow(userId);

        String color = WorkspaceColorUtil.resolveColor(
                request.color(),
                workspaceColorValidator,
                workspaceColorGenerator
        );

        Workspace workspace = Workspace.builder()
                .name(request.name())
                .color(color)
                .build();

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        // Workspace creator is always assigned OWNER role
        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                .workspace(savedWorkspace)
                .user(user)
                .role(WorkspaceRole.OWNER)
                .build();

        workspaceUserRepository.save(workspaceUser);

        // Create initial invite code for the workspace
        WorkspaceCode workspaceCode = WorkspaceCode.builder()
                .workspace(savedWorkspace)
                .code(workspaceCodeGenerator.generate())
                .build();

        workspaceCodeRepository.save(workspaceCode);

        return workspaceMapper.toWorkspaceResponse(savedWorkspace);
    }

    @Override
    public List<WorkspaceListItemResponse> getUserWorkspaces(Long userId) {

        List<WorkspaceUser> memberships =
                workspaceUserRepository.findAllByUserId(userId);

        return memberships.stream()
                .map(workspaceUserMapper::toWorkspaceListItemResponse)
                .toList();
    }

    /** Returns workspace details only if the user belongs to the workspace */
    @Override
    public WorkspaceDetailResponse getWorkspaceById(Long workspaceId, Long userId) {

        workspaceLoader.loadOrThrow(workspaceId);

        WorkspaceUser membership =
                workspaceAuthorizationService.requireMembership(workspaceId, userId);

        return workspaceUserMapper.toWorkspaceDetailResponse(membership);
    }

    /**
     * Updates mutable workspace fields.
     * Only the OWNER is allowed to perform this operation.
     */
    @Override
    public WorkspaceResponse updateWorkspace(
            Long workspaceId,
            Long userId,
            WorkspaceUpdateRequest request) {

        WorkspaceColorUtil.validateIfPresent(request.color(),  workspaceColorValidator);

        Workspace workspace = loadWorkspaceWithOwnerAccess(workspaceId, userId);

        Optional.ofNullable(request.name())
                .filter(n -> !n.isBlank())
                .ifPresent(workspace::setName);

        Optional.ofNullable(request.color())
                .filter(c -> !c.isBlank())
                .ifPresent(workspace::setColor);

        return workspaceMapper.toWorkspaceResponse(workspaceRepository.save(workspace));
    }

    /**
     * Marks a workspace as archived.
     * Archived workspaces are immutable and eligible for deletion.
     */
    @Override
    public void archiveWorkspace(Long workspaceId, Long userId) {

        Workspace workspace = loadWorkspaceWithOwnerAccess(workspaceId, userId);

        workspace.setArchived(true);
        workspaceRepository.save(workspace);
    }

    /** Restores an archived workspace to active state */
    @Override
    public void unarchiveWorkspace(Long workspaceId, Long userId) {

        Workspace workspace = loadWorkspaceWithOwnerAccess(workspaceId, userId);

        workspace.setArchived(false);
        workspaceRepository.save(workspace);
    }

    /**
     * Permanently deletes a workspace.
     * The workspace must be archived beforehand.
     */
    @Override
    public void deleteWorkspace(Long workspaceId, Long userId) {

        Workspace workspace = loadWorkspaceWithOwnerAccess(workspaceId, userId);

        if (!workspace.getArchived()) {
            throw new WorkspaceNotArchivedException("Workspace must be archived before deletion");
        }

        workspaceRepository.delete(workspace);
    }

    @Override
    public WorkspaceCodeResponse getInviteCode(Long workspaceId, Long userId) {

        WorkspaceCode workspaceCode = loadWorkspaceCodeWithOwnerAccess(workspaceId, userId);

        return workspaceCodeMapper.toWorkspaceCodeResponse(workspaceCode);
    }

    @Transactional
    @Override
    public WorkspaceCodeResponse enableInviteCode(Long workspaceId, Long userId) {

        WorkspaceCode workspaceCode = loadWorkspaceCodeWithOwnerAccess(workspaceId, userId);

        workspaceCode.setEnabled(true);
        workspaceCodeRepository.save(workspaceCode);

        return workspaceCodeMapper.toWorkspaceCodeResponse(workspaceCode);
    }

    @Transactional
    @Override
    public WorkspaceCodeResponse disableInviteCode(Long workspaceId, Long userId) {

        WorkspaceCode workspaceCode = loadWorkspaceCodeWithOwnerAccess(workspaceId, userId);

        workspaceCode.setEnabled(false);
        workspaceCodeRepository.save(workspaceCode);

        return workspaceCodeMapper.toWorkspaceCodeResponse(workspaceCode);
    }

    @Transactional
    @Override
    public WorkspaceCodeResponse regenerateInviteCode(Long workspaceId, Long userId) {

        WorkspaceCode workspaceCode = loadWorkspaceCodeWithOwnerAccess(workspaceId, userId);

        workspaceCode.setCode(workspaceCodeGenerator.generate());
        workspaceCodeRepository.save(workspaceCode);

        return workspaceCodeMapper.toWorkspaceCodeResponse(workspaceCode);
    }

    /**
     * Joins a workspace using a valid and enabled invite code.
     * Creates a MEMBER relationship if the user is not already part of the workspace.
     */
    @Transactional
    @Override
    public WorkspaceJoinResponse joinWorkspace(String inviteCode, Long userId) {

        WorkspaceCode workspaceCode =
                workspaceCodeLoader.loadEnabledByCodeOrThrow(inviteCode);

        Workspace workspace = workspaceCode.getWorkspace();
        User user = userLoader.loadUserOrThrow(userId);

        if (workspaceUserRepository.existsByWorkspaceIdAndUserId(workspace.getId(), userId)) {
            throw new UserAlreadyInWorkspaceException(
                    "User is already in workspace"
            );
        }

        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                .workspace(workspace)
                .user(user)
                .role(WorkspaceRole.MEMBER)
                .build();

        workspaceUserRepository.save(workspaceUser);

        return workspaceUserMapper.toWorkspaceJoinResponse(workspaceUser);
    }

    @Override
    public List<WorkspaceMemberResponse> getWorkspaceUsers(Long workspaceId, Long userId) {

        requireAccessibleWorkspace(workspaceId, userId);

        return workspaceUserRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(workspaceUserMapper::toWorkspaceMemberResponse)
                .toList();
    }

    @Transactional
    @Override
    public void removeWorkspaceUser(Long workspaceId, Long memberId, Long userId) {

        loadWorkspaceWithOwnerAccess(workspaceId, userId);

        WorkspaceUser member = workspaceUserRepository
                .findByWorkspaceIdAndUserId(workspaceId, memberId)
                .orElseThrow(() -> new WorkspaceMemberNotFoundException(
                        "Workspace member not found"
                ));

        // Prevent removing the workspace owner
        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new WorkspaceOperationNotAllowedException(
                    "Workspace operation not allowed"
            );
        }

        workspaceUserRepository.delete(member);
    }

    @Override
    public void leaveWorkspace(Long workspaceId, Long userId) {

        requireAccessibleWorkspace(workspaceId, userId);

        WorkspaceUser member = workspaceAuthorizationService
                .requireMembership(workspaceId, userId);

        // Prevent the workspace owner from leaving the workspace
        if (member.getRole() == WorkspaceRole.OWNER) {
            throw new WorkspaceOperationNotAllowedException(
                    "Workspace operation not allowed"
            );
        }

        workspaceUserRepository.delete(member);
    }

    /**
     * Verifies that the workspace exists and that the user belongs to it.
     * Does not return the workspace entity.
     */
    private void  requireAccessibleWorkspace(Long workspaceId, Long userId) {

        workspaceLoader.loadOrThrow(workspaceId);

        workspaceAuthorizationService.requireMembership(workspaceId, userId);
    }

    /**
     * Loads a workspace and verifies that the user belongs to it
     * and has OWNER permissions.
     */
    private Workspace loadWorkspaceWithOwnerAccess(Long workspaceId, Long userId) {

        Workspace workspace = workspaceLoader.loadOrThrow(workspaceId);

        workspaceAuthorizationService.requireRole(workspaceId, userId, WorkspaceRole.OWNER);

        return workspace;
    }

    /**
     * Loads the invite code for a workspace and verifies that the user
     * belongs to the workspace and has OWNER permissions.
     */
    private WorkspaceCode loadWorkspaceCodeWithOwnerAccess(Long workspaceId, Long userId) {

        Workspace workspace = loadWorkspaceWithOwnerAccess(workspaceId, userId);
        return workspaceCodeLoader.loadByWorkspaceOrThrow(workspace);
    }
}
