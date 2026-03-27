package com.tempertime.tempertime_api.workspaces.service.core;

import com.tempertime.tempertime_api.common.hash.Hash;
import com.tempertime.tempertime_api.common.normalizer.InputNormalizer;
import com.tempertime.tempertime_api.common.pagination.PageMapper;
import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.common.pagination.PaginationValidator;
import com.tempertime.tempertime_api.events.service.access.EventAccessService;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.service.loader.UserLoader;
import com.tempertime.tempertime_api.workspaces.config.WorkspaceConstraintsProperties;
import com.tempertime.tempertime_api.workspaces.service.authorization.WorkspaceAccessService;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceUpdateRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.*;
import com.tempertime.tempertime_api.workspaces.exception.*;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceInviteCodeMapper;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceMapper;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceUserMapper;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceInviteCode;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceUser;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceInviteCodeRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceUserRepository;
import com.tempertime.tempertime_api.common.color.ColorGenerator;
import com.tempertime.tempertime_api.common.color.ColorUtil;
import com.tempertime.tempertime_api.common.color.ColorValidator;
import com.tempertime.tempertime_api.workspaces.service.invitation.WorkspaceInviteCodeGenerator;
import com.tempertime.tempertime_api.workspaces.service.loader.WorkspaceInviteCodeLoader;
import com.tempertime.tempertime_api.workspaces.service.security.WorkspaceInviteCodeSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkspaceServiceImpl implements WorkspaceService {

    // Repositories
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final WorkspaceInviteCodeRepository workspaceInviteCodeRepository;

    // Loaders / Services
    private final UserLoader userLoader;
    private final WorkspaceAccessService workspaceAccessService;
    private final WorkspaceInviteCodeLoader workspaceInviteCodeLoader;
    private final EventAccessService eventAccessService;
    private final WorkspaceInviteCodeSecurityService workspaceInviteCodeSecurityService;

    // Mappers
    private final WorkspaceMapper workspaceMapper;
    private final WorkspaceUserMapper workspaceUserMapper;
    private final WorkspaceInviteCodeMapper workspaceInviteCodeMapper;

    // Generators / Validators / Normalizers
    private final ColorGenerator colorGenerator;
    private final ColorValidator colorValidator;
    private final WorkspaceInviteCodeGenerator workspaceInviteCodeGenerator;
    private final InputNormalizer inputNormalizer;
    private final PaginationValidator paginationValidator;

    // Configuration Properties
    private final WorkspaceConstraintsProperties workspaceConstraintsProperties;

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
                .name(inputNormalizer.normalize(request.name()))
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
        String encryptedInviteCode = workspaceInviteCodeSecurityService.encrypt(rawInviteCode);
        String hashedInviteCode = hash(rawInviteCode);

        WorkspaceInviteCode workspaceInviteCode = WorkspaceInviteCode.builder()
                .workspace(savedWorkspace)
                .inviteCodeEncrypted(encryptedInviteCode)
                .inviteCodeHash(hashedInviteCode)
                .build();

        workspaceInviteCodeRepository.save(workspaceInviteCode);

        // Return raw code ONLY here
        return workspaceMapper.toWorkspaceCreateResponse(savedWorkspace, rawInviteCode);
    }

    @Override
    public PageResponse<WorkspaceListItemResponse> getUserWorkspaces(
            Long userId,
            Pageable pageable
    ) {

        Pageable validatedPageable = paginationValidator.validate(pageable);

        Page<WorkspaceUser> page =
                workspaceUserRepository.findAllByUserId(userId, validatedPageable);

        Page<WorkspaceListItemResponse> mappedPage =
                page.map(workspaceUserMapper::toWorkspaceListItemResponse);

        return PageMapper.toPageResponse(mappedPage);
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
                .map(inputNormalizer::normalize)
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
    @Transactional
    @Override
    public void deleteWorkspace(Long workspaceId, Long userId) {

        Workspace workspace =
                workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

        if (!workspace.getArchived()) {
            throw new WorkspaceNotArchivedException();
        }

        eventAccessService.removeAllWorkspaceEvents(workspace.getId());

        workspaceRepository.delete(workspace);
    }

    /**
     * Returns workspace invite code details for the owner of the workspace.
     */
    @Override
    public WorkspaceInviteCodeResponse getInviteCode(Long workspaceId, Long userId) {

        WorkspaceInviteCode workspaceInviteCode = loadWorkspaceInviteCodeWithOwnerAccess(workspaceId, userId);

        String rawInviteCode =
                workspaceInviteCodeSecurityService.decrypt(workspaceInviteCode.getInviteCodeEncrypted());

        return workspaceInviteCodeMapper.toWorkspaceInviteCodeResponse(workspaceInviteCode, rawInviteCode);
    }

    @Transactional
    @Override
    public WorkspaceInviteCodeStatusResponse activateInviteCode(Long workspaceId, Long userId) {

        WorkspaceInviteCode workspaceInviteCode = loadWorkspaceInviteCodeWithOwnerAccess(workspaceId, userId);

        workspaceInviteCode.setInviteEnabled(true);
        workspaceInviteCodeRepository.save(workspaceInviteCode);

        return workspaceInviteCodeMapper
                .toWorkspaceInviteCodeStatusResponse(workspaceInviteCode);
    }

    @Transactional
    @Override
    public WorkspaceInviteCodeStatusResponse deactivateInviteCode(Long workspaceId, Long userId) {

        WorkspaceInviteCode workspaceInviteCode = loadWorkspaceInviteCodeWithOwnerAccess(workspaceId, userId);

        workspaceInviteCode.setInviteEnabled(false);
        workspaceInviteCodeRepository.save(workspaceInviteCode);

        return workspaceInviteCodeMapper
                .toWorkspaceInviteCodeStatusResponse(workspaceInviteCode);
    }

    /**
     * Regenerates the workspace invite code.
     * The raw invite code is returned in the response only once; only the hashed value is persisted.
     */
    @Transactional
    @Override
    public WorkspaceInviteCodeResponse regenerateInviteCode(Long workspaceId, Long userId) {

        WorkspaceInviteCode workspaceInviteCode =
                loadWorkspaceInviteCodeWithOwnerAccess(workspaceId, userId);

        String rawInviteCode = workspaceInviteCodeGenerator.generate();
        String encryptedInviteCode = workspaceInviteCodeSecurityService.encrypt(rawInviteCode);
        String hashedInviteCode = hash(rawInviteCode);

        workspaceInviteCode.setInviteCodeEncrypted(encryptedInviteCode);
        workspaceInviteCode.setInviteCodeHash(hashedInviteCode);
        workspaceInviteCode.setLastRegeneratedAt(Instant.now());

        workspaceInviteCodeRepository.save(workspaceInviteCode);

        return workspaceInviteCodeMapper
                .toWorkspaceInviteCodeResponse(workspaceInviteCode, rawInviteCode);
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
            throw new UserAlreadyInWorkspaceException();
        }

        validateWorkspaceCapacity(workspace);

        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                .workspace(workspace)
                .user(user)
                .build();

        workspaceUserRepository.save(workspaceUser);

        eventAccessService.assignUserToGlobalEvents(
                workspace.getId(),
                user.getId()
        );

        return workspaceUserMapper.toWorkspaceJoinResponse(workspaceUser);
    }

    @Override
    public PageResponse<WorkspaceUserResponse> getWorkspaceUsers(
            Long workspaceId,
            Long userId,
            Pageable pageable
    ) {

        Pageable validatedPageable = paginationValidator.validate(pageable);

        workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId);

        Page<WorkspaceUser> page =
                workspaceUserRepository.findByWorkspaceId(workspaceId, validatedPageable);

        Page<WorkspaceUserResponse> mappedPage =
                page.map(workspaceUserMapper::toWorkspaceUserResponse);

        return PageMapper.toPageResponse(mappedPage);
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
                .orElseThrow(WorkspaceUserNotFoundException::new);

        // Prevent removing the workspace owner
        if (workspaceUser.getRole() == WorkspaceRole.OWNER) {
            throw new WorkspaceOperationNotAllowedException();
        }

        eventAccessService.removeUserFromWorkspaceEvents(
                workspaceId,
                targetUserId
        );

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
            throw new WorkspaceOperationNotAllowedException();
        }

        eventAccessService.removeUserFromWorkspaceEvents(
                workspaceId,
                userId
        );

        workspaceUserRepository.delete(workspaceUser);
    }

    /**
     * Validates that the workspace has not exceeded the maximum allowed users.
     */
    private void validateWorkspaceCapacity(Workspace workspace) {

        int maxCapacity = workspaceConstraintsProperties.getMaxUsers();
        long currentUserCount = workspaceUserRepository.countByWorkspaceId(workspace.getId());

        if (currentUserCount >= maxCapacity) {
            throw new WorkspaceCapacityExceededException();
        }
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

    /**
     * Hashes the raw invite code using SHA-256.
     */
    private String hash(String rawInviteCode) {

        return Hash.sha256(rawInviteCode);
    }
}
