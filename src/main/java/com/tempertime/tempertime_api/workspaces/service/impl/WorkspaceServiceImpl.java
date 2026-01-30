package com.tempertime.tempertime_api.workspaces.service.impl;

import com.tempertime.tempertime_api.users.exception.UserNotFoundException;
import com.tempertime.tempertime_api.users.model.User;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceUpdateRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceDetailResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceListItemResponse;
import com.tempertime.tempertime_api.workspaces.dto.response.WorkspaceResponse;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceAccessDeniedException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceNotArchivedException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceNotFoundException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceRoleDeniedException;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceMapper;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceUserMapper;
import com.tempertime.tempertime_api.workspaces.model.Workspace;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceUser;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceUserRepository;
import com.tempertime.tempertime_api.workspaces.service.WorkspaceService;
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
    private final UserRepository userRepository;

    /**
     * Creates a new workspace and assigns the creator as OWNER.
     * If no color is provided, one is automatically resolved.
     */
    @Transactional
    @Override
    public WorkspaceResponse createWorkspace(WorkspaceCreateRequest request, Long userId) {

        String color = WorkspaceColorUtil.resolveColor(
                request.color(),
                workspaceColorValidator,
                workspaceColorGenerator
        );

        Workspace workspace = Workspace.builder()
                .name(request.name())
                .color(color)
                .build();

        User user = loadUserOrThrow(userId);

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        // Workspace creator is always assigned OWNER role
        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                .workspace(savedWorkspace)
                .user(user)
                .role(WorkspaceRole.OWNER)
                .build();

        workspaceUserRepository.save(workspaceUser);

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

        workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        // Throws 403 if user is not a member
        WorkspaceUser membership = workspaceUserRepository
                .findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() ->
                        new WorkspaceAccessDeniedException("Workspace not accessible"));

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

        if (Boolean.FALSE.equals(workspace.getArchived())) {
            throw new WorkspaceNotArchivedException("Workspace must be archived before deletion");
        }

        workspaceRepository.delete(workspace);
    }

    /** Loads a workspace if the user exists and is OWNER, otherwise throws relevant exception */
    private Workspace loadWorkspaceForOwner(Long workspaceId, Long userId) {

        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found"));

        boolean userInWorkspace = workspaceUserRepository.existsByWorkspaceIdAndUserId(workspaceId, userId);
        if (!userInWorkspace) {
            throw new WorkspaceAccessDeniedException("Workspace not accessible");
        }

        boolean isOwner = workspaceUserRepository
                .existsByWorkspaceIdAndUserIdAndRole(workspaceId, userId, WorkspaceRole.OWNER);
        if (!isOwner) {
            throw new WorkspaceRoleDeniedException("User does not have sufficient permissions");
        }

        return workspace;
    }

    private User loadUserOrThrow(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User not found with id: " + userId)
                );
    }
}
