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
import com.tempertime.tempertime_api.workspaces.service.WorkspaceAuthorizationService;
import com.tempertime.tempertime_api.workspaces.service.WorkspaceLoader;
import com.tempertime.tempertime_api.workspaces.service.WorkspaceService;
import com.tempertime.tempertime_api.workspaces.service.WorkspaceCodeGenerator;
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

    /** Returns workspace details only if the user is a member */
    @Override
    public WorkspaceDetailResponse getWorkspaceById(Long workspaceId, Long userId) {

        workspaceLoader.loadOrThrow(workspaceId);

        WorkspaceUser membership =
                workspaceAuthorizationService.requireMemberAndGet(workspaceId, userId);

        return workspaceUserMapper.toWorkspaceDetailResponse(membership);
    }

    /**
     * Updates mutable workspace fields.
     * Only the OWNER is allowed to perform this operation.
     */
    @Override
    public WorkspaceResponse updateWorkspace(Long workspaceId,
                                             Long userId,
                                             WorkspaceUpdateRequest request) {

        WorkspaceColorUtil.validateIfPresent(request.color(),  workspaceColorValidator);

        Workspace workspace = loadWorkspaceForOwner(workspaceId, userId);

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

        Workspace workspace = loadWorkspaceForOwner(workspaceId, userId);

        workspace.setArchived(true);
        workspaceRepository.save(workspace);
    }

    /** Restores an archived workspace to active state */
    @Override
    public void unarchiveWorkspace(Long workspaceId, Long userId) {

        Workspace workspace = loadWorkspaceForOwner(workspaceId, userId);

        workspace.setArchived(false);
        workspaceRepository.save(workspace);
    }

    /**
     * Permanently deletes a workspace.
     * The workspace must be archived beforehand.
     */
    @Override
    public void deleteWorkspace(Long workspaceId, Long userId) {

        Workspace workspace = loadWorkspaceForOwner(workspaceId, userId);

        if (!workspace.getArchived()) {
            throw new WorkspaceNotArchivedException("Workspace must be archived before deletion");
        }

        workspaceRepository.delete(workspace);
    }

    @Override
    public WorkspaceCodeResponse getInviteCode(Long workspaceId, Long userId) {

        loadWorkspaceForOwner(workspaceId, userId);

        WorkspaceCode workspaceCode = loadWorkspaceCodeByWorkspaceIdOrThrow(workspaceId);

        return workspaceCodeMapper.toWorkspaceCodeResponse(workspaceCode);
    }

    @Override
    public WorkspaceCodeResponse enableInviteCode(Long workspaceId, Long userId) {

        loadWorkspaceForOwner(workspaceId, userId);

        WorkspaceCode workspaceCode = loadWorkspaceCodeByWorkspaceIdOrThrow(workspaceId);

        workspaceCode.setEnabled(true);
        workspaceCodeRepository.save(workspaceCode);

        return workspaceCodeMapper.toWorkspaceCodeResponse(workspaceCode);
    }

    @Override
    public WorkspaceCodeResponse disableInviteCode(Long workspaceId, Long userId) {

        loadWorkspaceForOwner(workspaceId, userId);

        WorkspaceCode workspaceCode = loadWorkspaceCodeByWorkspaceIdOrThrow(workspaceId);

        workspaceCode.setEnabled(false);
        workspaceCodeRepository.save(workspaceCode);

        return workspaceCodeMapper.toWorkspaceCodeResponse(workspaceCode);
    }

    @Transactional
    @Override
    public WorkspaceCodeResponse regenerateInviteCode(Long workspaceId, Long userId) {

        loadWorkspaceForOwner(workspaceId, userId);

        WorkspaceCode workspaceCode = loadWorkspaceCodeByWorkspaceIdOrThrow(workspaceId);

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

        WorkspaceCode workspaceCode = workspaceCodeRepository.findByCode(inviteCode)
                .orElseThrow(() -> new InvalidWorkspaceInviteCodeException(
                        "Invalid workspace invite code"
                ));

        if (!workspaceCode.getEnabled()) {
            throw new WorkspaceInviteCodeDisabledException(
                    "Workspace invite code is disabled"
            );
        }

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

    /** Loads a workspace with OWNER authorization */
    private Workspace loadWorkspaceForOwner(Long workspaceId, Long userId) {

        Workspace workspace = workspaceLoader.loadOrThrow(workspaceId);

        workspaceAuthorizationService.requireMember(workspaceId, userId);
        workspaceAuthorizationService.requireOwner(workspaceId, userId);

        return workspace;
    }

    /** Loads the workspace invite code using the workspace ID */
    private WorkspaceCode loadWorkspaceCodeByWorkspaceIdOrThrow(Long workspaceId) {

        return workspaceCodeRepository.findByWorkspaceId(workspaceId)
                .orElseThrow(() -> new WorkspaceInviteCodeNotFoundException(
                        "Workspace invite code not found"
                ));
    }
}
