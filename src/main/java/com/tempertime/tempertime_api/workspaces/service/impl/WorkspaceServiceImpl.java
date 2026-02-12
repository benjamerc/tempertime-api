package com.tempertime.tempertime_api.workspaces.service.impl;

import com.tempertime.tempertime_api.common.hash.Hash;
import com.tempertime.tempertime_api.events.model.Event;
import com.tempertime.tempertime_api.events.model.EventScope;
import com.tempertime.tempertime_api.events.model.EventUser;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.events.repository.EventUserRepository;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.service.UserLoader;
import com.tempertime.tempertime_api.workspaces.access.WorkspaceAccessService;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceUpdateRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.*;
import com.tempertime.tempertime_api.workspaces.exception.*;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceInviteCodeMapper;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceMapper;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceUserMapper;
import com.tempertime.tempertime_api.workspaces.model.Workspace;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceInviteCode;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.model.WorkspaceUser;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceInviteCodeRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceUserRepository;
import com.tempertime.tempertime_api.workspaces.service.*;
import com.tempertime.tempertime_api.common.color.ColorGenerator;
import com.tempertime.tempertime_api.common.color.ColorUtil;
import com.tempertime.tempertime_api.common.color.ColorValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    // Repositories
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final WorkspaceInviteCodeRepository workspaceInviteCodeRepository;
    private final EventRepository eventRepository;
    private final EventUserRepository eventUserRepository;

    // Loaders / Services
    private final UserLoader userLoader;
    private final WorkspaceAccessService workspaceAccessService;
    private final WorkspaceInviteCodeLoader workspaceInviteCodeLoader;

    // Mappers
    private final WorkspaceMapper workspaceMapper;
    private final WorkspaceUserMapper workspaceUserMapper;
    private final WorkspaceInviteCodeMapper workspaceInviteCodeMapper;

    // Generators / Validators
    private final ColorGenerator colorGenerator;
    private final ColorValidator colorValidator;
    private final WorkspaceInviteCodeGenerator workspaceInviteCodeGenerator;

    /**
     * Creates a new workspace and assigns the creator as OWNER.
     * If no color is provided, it is automatically resolved.
     * Generates an initial workspace invite code.
     * The raw invite code is returned in the response only once; only the hashed value is persisted.
     */
    @Transactional
    @Override
    public WorkspaceCreateResponse createWorkspace(WorkspaceCreateRequest request, Long userId) {

        User user = userLoader.loadUserOrThrow(userId);

        String color = ColorUtil.resolveColor(
                request.color(),
                colorValidator,
                colorGenerator
        );

        Workspace workspace = Workspace.builder()
                .name(request.name())
                .color(color)
                .build();

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        // Creator is OWNER
        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                .workspace(savedWorkspace)
                .user(user)
                .role(WorkspaceRole.OWNER)
                .build();

        workspaceUserRepository.save(workspaceUser);

        // Generate initial workspace invite code (raw code returned only once)
        String rawInviteCode = workspaceInviteCodeGenerator.generate();
        String inviteCodeHash = hash(rawInviteCode);

        WorkspaceInviteCode workspaceInviteCode = WorkspaceInviteCode.builder()
                .workspace(savedWorkspace)
                .inviteCodeHash(inviteCodeHash)
                .build();

        workspaceInviteCodeRepository.save(workspaceInviteCode);

        // Return raw code ONLY here
        return workspaceMapper.toWorkspaceCreateResponse(savedWorkspace, rawInviteCode);
    }

    @Override
    public List<WorkspaceListItemResponse> getUserWorkspaces(Long userId) {

        List<WorkspaceUser> workspaceUsers =
                workspaceUserRepository.findAllByUserId(userId);

        return workspaceUsers.stream()
                .map(workspaceUserMapper::toWorkspaceListItemResponse)
                .toList();
    }

    /**
     * Returns workspace details only if the user belongs to the workspace
     */
    @Override
    public WorkspaceDetailResponse getWorkspaceById(Long workspaceId, Long userId) {

        WorkspaceUser workspaceUser = workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId);

        return workspaceUserMapper.toWorkspaceDetailResponse(workspaceUser);
    }

    /**
     * Updates mutable workspace fields.
     * Only the OWNER is allowed to perform this operation.
     */
    @Override
    public WorkspaceUpdateResponse updateWorkspace(
            Long workspaceId,
            Long userId,
            WorkspaceUpdateRequest request) {

        ColorUtil.validateIfPresent(request.color(), colorValidator);

        Workspace workspace =
                workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

        Optional.ofNullable(request.name())
                .filter(n -> !n.isBlank())
                .ifPresent(workspace::setName);

        Optional.ofNullable(request.color())
                .filter(c -> !c.isBlank())
                .ifPresent(workspace::setColor);

        return workspaceMapper.toWorkspaceUpdateResponse(workspaceRepository.save(workspace));
    }

    /**
     * Marks a workspace as archived.
     * Archived workspaces are immutable and eligible for deletion.
     */
    @Override
    public void archiveWorkspace(Long workspaceId, Long userId) {

        Workspace workspace =
                workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

        workspace.setArchived(true);
        workspaceRepository.save(workspace);
    }

    /**
     * Restores an archived workspace to active state
     */
    @Override
    public void unarchiveWorkspace(Long workspaceId, Long userId) {

        Workspace workspace =
                workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

        workspace.setArchived(false);
        workspaceRepository.save(workspace);
    }

    /**
     * Permanently deletes a workspace.
     * The workspace must be archived beforehand.
     */
    @Override
    public void deleteWorkspace(Long workspaceId, Long userId) {

        Workspace workspace =
                workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

        if (!workspace.getArchived()) {
            throw new WorkspaceNotArchivedException("Workspace must be archived before deletion");
        }

        workspaceRepository.delete(workspace);
    }

    /**
     * Returns workspace invite code metadata (enabled status and creation time).
     * The raw invite code is not exposed for security reasons.
     */
    @Override
    public WorkspaceInviteCodeResponse getInviteCode(Long workspaceId, Long userId) {

        WorkspaceInviteCode workspaceInviteCode = loadWorkspaceInviteCodeWithOwnerAccess(workspaceId, userId);

        return workspaceInviteCodeMapper.toWorkspaceInviteCodeResponse(workspaceInviteCode);
    }

    @Transactional
    @Override
    public WorkspaceInviteCodeResponse activateInviteCode(Long workspaceId, Long userId) {

        WorkspaceInviteCode workspaceInviteCode = loadWorkspaceInviteCodeWithOwnerAccess(workspaceId, userId);

        workspaceInviteCode.setInviteEnabled(true);
        workspaceInviteCodeRepository.save(workspaceInviteCode);

        return workspaceInviteCodeMapper.toWorkspaceInviteCodeResponse(workspaceInviteCode);
    }

    @Transactional
    @Override
    public WorkspaceInviteCodeResponse deactivateInviteCode(Long workspaceId, Long userId) {

        WorkspaceInviteCode workspaceInviteCode = loadWorkspaceInviteCodeWithOwnerAccess(workspaceId, userId);

        workspaceInviteCode.setInviteEnabled(false);
        workspaceInviteCodeRepository.save(workspaceInviteCode);

        return workspaceInviteCodeMapper.toWorkspaceInviteCodeResponse(workspaceInviteCode);
    }

    /**
     * Regenerates the workspace invite code.
     * The raw invite code is returned in the response only once; only the hashed value is persisted.
     */
    @Transactional
    @Override
    public WorkspaceInviteCodeRegenerateResponse regenerateInviteCode(Long workspaceId, Long userId) {

        WorkspaceInviteCode workspaceInviteCode =
                loadWorkspaceInviteCodeWithOwnerAccess(workspaceId, userId);

        String rawInviteCode = workspaceInviteCodeGenerator.generate();
        String inviteCodeHash = hash(rawInviteCode);

        workspaceInviteCode.setInviteCodeHash(inviteCodeHash);
        workspaceInviteCodeRepository.save(workspaceInviteCode);

        return workspaceInviteCodeMapper
                .toWorkspaceInviteCodeRegenerateResponse(workspaceInviteCode, rawInviteCode);
    }

    /**
     * Joins a workspace using a valid invite code, creates a MEMBER relationship,
     * and assigns the user to all global events.
     */
    @Transactional
    @Override
    public WorkspaceJoinResponse joinWorkspace(String inviteCode, Long userId) {

        WorkspaceInviteCode workspaceInviteCode =
                workspaceInviteCodeLoader.loadEnabledByCodeOrThrow(inviteCode);

        Workspace workspace = workspaceInviteCode.getWorkspace();
        User user = userLoader.loadUserOrThrow(userId);

        if (workspaceUserRepository.existsByWorkspaceIdAndUserId(workspace.getId(), userId)) {
            throw new UserAlreadyInWorkspaceException(
                    "User is already in workspace"
            );
        }

        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                .workspace(workspace)
                .user(user)
                .build();

        workspaceUserRepository.save(workspaceUser);

        assignUserToGlobalEvents(workspace, user);

        return workspaceUserMapper.toWorkspaceJoinResponse(workspaceUser);
    }

    @Override
    public List<WorkspaceUserResponse> getWorkspaceUsers(Long workspaceId, Long userId) {

        workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId);

        return workspaceUserRepository.findByWorkspaceId(workspaceId)
                .stream()
                .map(workspaceUserMapper::toWorkspaceUserResponse)
                .toList();
    }

    /**
     * Removes a user from a workspace. Must be performed by the OWNER.
     * Owners cannot remove themselves.
     */
    @Transactional
    @Override
    public void removeWorkspaceUser(Long workspaceId, Long targetUserId, Long userId) {

        workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new WorkspaceUserNotFoundException(
                        "Workspace member not found"
                ));

        // Prevent removing the workspace owner
        if (workspaceUser.getRole() == WorkspaceRole.OWNER) {
            throw new WorkspaceOperationNotAllowedException(
                    "Workspace operation not allowed"
            );
        }

        removeUserFromWorkspaceEvents(workspaceId,  targetUserId);

        workspaceUserRepository.delete(workspaceUser);
    }

    /**
     * Allows a workspace user to leave the workspace.
     * Owners cannot leave themselves.
     */
    @Transactional
    @Override
    public void leaveWorkspace(Long workspaceId, Long userId) {

        WorkspaceUser workspaceUser =
                workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId);

        // Prevent the workspace owner from leaving the workspace
        if (workspaceUser.getRole() == WorkspaceRole.OWNER) {
            throw new WorkspaceOperationNotAllowedException(
                    "Workspace operation not allowed"
            );
        }

        removeUserFromWorkspaceEvents(workspaceId, userId);

        workspaceUserRepository.delete(workspaceUser);
    }

    /**
     * Loads the workspace invite code and verifies that the user
     * belongs to the workspace and has OWNER permissions.
     * Throws a domain-specific exception if the workspace does not exist
     * or the user is not an OWNER.
     */
    private WorkspaceInviteCode loadWorkspaceInviteCodeWithOwnerAccess(Long workspaceId, Long userId) {

        Workspace workspace =
                workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);
        return workspaceInviteCodeLoader.loadByWorkspaceOrThrow(workspace);
    }

    /** Hashes the raw invite code using SHA-256 */
    private String hash(String rawInviteCode) {

        return Hash.sha256(rawInviteCode);
    }

    /** Assigns the user to all global events in the workspace */
    private void assignUserToGlobalEvents(Workspace workspace, User user) {

        List<Event> globalEvents = eventRepository
                .findByWorkspaceIdAndScope(workspace.getId(), EventScope.GLOBAL);

        List<EventUser> eventUsers = globalEvents.stream()
                .map(event -> EventUser.builder()
                        .event(event)
                        .user(user)
                        .build())
                .toList();

        eventUserRepository.saveAll(eventUsers);
    }

    /** Removes the user from all events in the workspace */
    private void removeUserFromWorkspaceEvents(Long workspaceId, Long userId) {

        eventUserRepository.deleteByEventWorkspaceIdAndUserId(workspaceId, userId);
    }
}
