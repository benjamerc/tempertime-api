package com.tempertime.tempertime_api.workspaces.service.core;

import com.tempertime.tempertime_api.common.color.ColorGenerator;
import com.tempertime.tempertime_api.common.color.ColorValidator;
import com.tempertime.tempertime_api.common.color.InvalidColorFormatException;
import com.tempertime.tempertime_api.common.normalizer.InputNormalizer;
import com.tempertime.tempertime_api.common.pagination.PageResponse;
import com.tempertime.tempertime_api.common.pagination.PaginationValidator;
import com.tempertime.tempertime_api.events.service.access.EventAccessService;
import com.tempertime.tempertime_api.users.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.exception.UserNotFoundException;
import com.tempertime.tempertime_api.users.service.loader.UserLoader;
import com.tempertime.tempertime_api.workspaces.data.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.config.WorkspaceConstraintsProperties;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceInviteCode;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceUser;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceCreateRequest;
import com.tempertime.tempertime_api.workspaces.dto.request.WorkspaceUpdateRequest;
import com.tempertime.tempertime_api.workspaces.dto.response.*;
import com.tempertime.tempertime_api.workspaces.exception.*;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceInviteCodeMapper;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceMapper;
import com.tempertime.tempertime_api.workspaces.mapper.WorkspaceUserMapper;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceInviteCodeRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceRepository;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceUserRepository;
import com.tempertime.tempertime_api.workspaces.service.authorization.WorkspaceAccessService;
import com.tempertime.tempertime_api.workspaces.service.invitation.WorkspaceInviteCodeGenerator;
import com.tempertime.tempertime_api.workspaces.service.loader.WorkspaceInviteCodeLoader;
import com.tempertime.tempertime_api.workspaces.service.security.WorkspaceInviteCodeSecurityService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkspaceServiceImplTest {

    // Repositories
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private WorkspaceUserRepository workspaceUserRepository;
    @Mock
    private WorkspaceInviteCodeRepository workspaceInviteCodeRepository;

    // Loaders / Services
    @Mock
    private UserLoader userLoader;
    @Mock
    private WorkspaceAccessService workspaceAccessService;
    @Mock
    private WorkspaceInviteCodeLoader workspaceInviteCodeLoader;
    @Mock
    private EventAccessService eventAccessService;
    @Mock
    private WorkspaceInviteCodeSecurityService workspaceInviteCodeSecurityService;

    // Mappers
    @Mock
    private WorkspaceMapper workspaceMapper;
    @Mock
    private WorkspaceUserMapper workspaceUserMapper;
    @Mock
    private WorkspaceInviteCodeMapper workspaceInviteCodeMapper;

    // Generators / Validators / Normalizers
    @Mock
    private ColorGenerator colorGenerator;
    @Mock
    private ColorValidator colorValidator;
    @Mock
    private WorkspaceInviteCodeGenerator workspaceInviteCodeGenerator;
    @Mock
    private InputNormalizer inputNormalizer;
    @Mock
    private PaginationValidator paginationValidator;

    // Configuration Properties
    @Mock
    private WorkspaceConstraintsProperties workspaceConstraintsProperties;

    // Class under test
    @InjectMocks
    private WorkspaceServiceImpl workspaceService;

    @Nested
    class CreateWorkspaceTests {

        @Test
        void shouldCreateWorkspace_whenValidDataProvided() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);
            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            WorkspaceCreateRequest request = new WorkspaceCreateRequest(WorkspaceTestDataProvider.NAME, WorkspaceTestDataProvider.COLOR);
            WorkspaceCreateResponse response = WorkspaceTestDataProvider.workspaceCreateResponse(workspace);

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);
            when(colorValidator.isColorMissing(request.color())).thenReturn(false);
            when(colorValidator.isHexColor(request.color())).thenReturn(true);
            when(inputNormalizer.normalize(request.name())).thenReturn(request.name());
            when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);
            when(workspaceInviteCodeGenerator.generate()).thenReturn(WorkspaceTestDataProvider.INVITE_CODE);
            when(workspaceInviteCodeSecurityService.encrypt(WorkspaceTestDataProvider.INVITE_CODE))
                    .thenReturn(WorkspaceTestDataProvider.INVITE_CODE_ENCRYPTED);
            when(workspaceMapper.toWorkspaceCreateResponse(eq(workspace), any())).thenReturn(response);

            WorkspaceCreateResponse result = workspaceService.createWorkspace(request, userId);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(response);

            verify(userLoader).loadUserOrThrow(userId);
            verify(colorValidator).isColorMissing(request.color());
            verify(colorValidator).isHexColor(request.color());
            verify(colorGenerator, never()).generate();
            verify(inputNormalizer).normalize(request.name());
            verify(workspaceRepository).save(any(Workspace.class));
            verify(workspaceUserRepository).save(any(WorkspaceUser.class));
            verify(workspaceInviteCodeGenerator).generate();
            verify(workspaceInviteCodeSecurityService).encrypt(WorkspaceTestDataProvider.INVITE_CODE);
            verify(workspaceInviteCodeRepository).save(any(WorkspaceInviteCode.class));
            verify(workspaceMapper).toWorkspaceCreateResponse(eq(workspace), any());
        }

        @Test
        void shouldCreateWorkspace_whenNoColorProvided() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);
            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            WorkspaceCreateRequest request = new WorkspaceCreateRequest(WorkspaceTestDataProvider.NAME, null);
            WorkspaceCreateResponse response = WorkspaceTestDataProvider.workspaceCreateResponse(workspace);

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);
            when(colorValidator.isColorMissing(null)).thenReturn(true);
            when(colorGenerator.generate()).thenReturn(WorkspaceTestDataProvider.COLOR);
            when(inputNormalizer.normalize(request.name())).thenReturn(request.name());
            when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);
            when(workspaceInviteCodeGenerator.generate()).thenReturn(WorkspaceTestDataProvider.INVITE_CODE);
            when(workspaceInviteCodeSecurityService.encrypt(WorkspaceTestDataProvider.INVITE_CODE))
                    .thenReturn(WorkspaceTestDataProvider.INVITE_CODE_ENCRYPTED);
            when(workspaceMapper.toWorkspaceCreateResponse(eq(workspace), any())).thenReturn(response);

            WorkspaceCreateResponse result = workspaceService.createWorkspace(request, userId);

            assertThat(result).isNotNull();

            verify(colorValidator).isColorMissing(null);
            verify(colorGenerator).generate();
            verify(colorValidator, never()).isHexColor(any());
        }

        @Test
        void shouldThrowInvalidColorFormatException_whenColorFormatIsInvalid() {

            Long userId = 1L;

            User user = UserTestDataProvider.user(userId);
            WorkspaceCreateRequest request = new WorkspaceCreateRequest(WorkspaceTestDataProvider.NAME, "rojo");

            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);
            when(colorValidator.isColorMissing("rojo")).thenReturn(false);
            when(colorValidator.isHexColor("rojo")).thenReturn(false);

            assertThatThrownBy(() -> workspaceService.createWorkspace(request, userId))
                    .isInstanceOf(InvalidColorFormatException.class);

            verify(colorValidator).isColorMissing("rojo");
            verify(colorValidator).isHexColor("rojo");
            verify(workspaceRepository, never()).save(any());
            verify(workspaceUserRepository, never()).save(any());
            verify(workspaceInviteCodeRepository, never()).save(any());
        }

        @Test
        void shouldThrowUserNotFoundException_whenUserDoesNotExist() {

            Long userId = 99L;
            WorkspaceCreateRequest request = new WorkspaceCreateRequest(WorkspaceTestDataProvider.NAME, WorkspaceTestDataProvider.COLOR);

            when(userLoader.loadUserOrThrow(userId)).thenThrow(new UserNotFoundException());

            assertThatThrownBy(() -> workspaceService.createWorkspace(request, userId))
                    .isInstanceOf(UserNotFoundException.class);

            verify(userLoader).loadUserOrThrow(userId);
            verify(colorValidator, never()).isColorMissing(any());
            verify(workspaceRepository, never()).save(any());
            verify(workspaceUserRepository, never()).save(any());
            verify(workspaceInviteCodeRepository, never()).save(any());
        }
    }

    @Nested
    class GetUserWorkspacesTests {

        @Test
        void shouldReturnPagedWorkspaces_whenUserHasWorkspaces() {

            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            User user = UserTestDataProvider.user(userId);
            WorkspaceUser workspaceUser = WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, user);
            WorkspaceListItemResponse listItemResponse =
                    WorkspaceTestDataProvider.workspaceListItemResponse(workspace, WorkspaceRole.OWNER);

            Page<WorkspaceUser> page = new PageImpl<>(List.of(workspaceUser));

            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(workspaceUserRepository.findWorkspacesByUserAndOptionalFilters(userId, null, null, pageable))
                    .thenReturn(page);
            when(workspaceUserMapper.toWorkspaceListItemResponse(workspaceUser)).thenReturn(listItemResponse);

            PageResponse<WorkspaceListItemResponse> result =
                    workspaceService.getUserWorkspaces(userId, null, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0)).usingRecursiveComparison().isEqualTo(listItemResponse);

            verify(paginationValidator).validate(pageable);
            verify(workspaceUserRepository).findWorkspacesByUserAndOptionalFilters(userId, null, null, pageable);
            verify(workspaceUserMapper).toWorkspaceListItemResponse(workspaceUser);
        }

        @Test
        void shouldReturnEmptyPage_whenUserHasNoWorkspaces() {

            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(workspaceUserRepository.findWorkspacesByUserAndOptionalFilters(userId, null, null, pageable))
                    .thenReturn(Page.empty());

            PageResponse<WorkspaceListItemResponse> result =
                    workspaceService.getUserWorkspaces(userId, null, null, pageable);

            assertThat(result).isNotNull();
            assertThat(result.content()).isEmpty();

            verify(paginationValidator).validate(pageable);
            verify(workspaceUserRepository).findWorkspacesByUserAndOptionalFilters(userId, null, null, pageable);
            verify(workspaceUserMapper, never()).toWorkspaceListItemResponse(any());
        }
    }

    @Nested
    class GetWorkspaceByIdTests {

        @Test
        void shouldReturnWorkspaceDetail_whenUserHasAccess() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            WorkspaceUser workspaceUser = WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, user);
            WorkspaceDetailResponse response =
                    WorkspaceTestDataProvider.workspaceDetailResponse(workspace, WorkspaceRole.OWNER);

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId)).thenReturn(workspaceUser);
            when(workspaceUserMapper.toWorkspaceDetailResponse(workspaceUser)).thenReturn(response);

            WorkspaceDetailResponse result = workspaceService.getWorkspaceById(workspaceId, userId);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(response);

            verify(workspaceAccessService).requireAccessibleWorkspace(workspaceId, userId);
            verify(workspaceUserMapper).toWorkspaceDetailResponse(workspaceUser);
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceService.getWorkspaceById(workspaceId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(workspaceAccessService).requireAccessibleWorkspace(workspaceId, userId);
            verify(workspaceUserMapper, never()).toWorkspaceDetailResponse(any());
        }

        @Test
        void shouldThrowWorkspaceAccessDeniedException_whenUserHasNoAccess() {

            Long workspaceId = 1L;
            Long userId = 99L;

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenThrow(new WorkspaceAccessDeniedException());

            assertThatThrownBy(() -> workspaceService.getWorkspaceById(workspaceId, userId))
                    .isInstanceOf(WorkspaceAccessDeniedException.class);

            verify(workspaceAccessService).requireAccessibleWorkspace(workspaceId, userId);
            verify(workspaceUserMapper, never()).toWorkspaceDetailResponse(any());
        }
    }

    @Nested
    class UpdateWorkspaceTests {

        @Test
        void shouldUpdateBothFields_whenValidDataProvided() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            WorkspaceUpdateRequest request = new WorkspaceUpdateRequest("UpdatedName", "#B2C3D4");
            WorkspaceUpdateResponse response = WorkspaceTestDataProvider.workspaceUpdateResponse(workspace);

            when(colorValidator.isColorMissing("#B2C3D4")).thenReturn(false);
            when(colorValidator.isHexColor("#B2C3D4")).thenReturn(true);
            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(inputNormalizer.normalize("UpdatedName")).thenReturn("UpdatedName");
            when(workspaceRepository.save(workspace)).thenReturn(workspace);
            when(workspaceMapper.toWorkspaceUpdateResponse(workspace)).thenReturn(response);

            WorkspaceUpdateResponse result = workspaceService.updateWorkspace(workspaceId, userId, request);

            assertThat(result).isNotNull();
            assertThat(workspace.getName()).isEqualTo("UpdatedName");
            assertThat(workspace.getColor()).isEqualTo("#B2C3D4");

            verify(colorValidator).isColorMissing("#B2C3D4");
            verify(colorValidator).isHexColor("#B2C3D4");
            verify(inputNormalizer).normalize("UpdatedName");
            verify(workspaceRepository).save(workspace);
            verify(workspaceMapper).toWorkspaceUpdateResponse(workspace);
        }

        @Test
        void shouldUpdateOnlyName_whenColorIsNull() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            String originalColor = workspace.getColor();
            WorkspaceUpdateRequest request = new WorkspaceUpdateRequest("UpdatedName", null);
            WorkspaceUpdateResponse response = WorkspaceTestDataProvider.workspaceUpdateResponse(workspace);

            when(colorValidator.isColorMissing(null)).thenReturn(true);
            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(inputNormalizer.normalize("UpdatedName")).thenReturn("UpdatedName");
            when(workspaceRepository.save(workspace)).thenReturn(workspace);
            when(workspaceMapper.toWorkspaceUpdateResponse(workspace)).thenReturn(response);

            workspaceService.updateWorkspace(workspaceId, userId, request);

            assertThat(workspace.getName()).isEqualTo("UpdatedName");
            assertThat(workspace.getColor()).isEqualTo(originalColor);

            verify(colorValidator).isColorMissing(null);
            verify(colorValidator, never()).isHexColor(any());
            verify(inputNormalizer).normalize("UpdatedName");
            verify(workspaceRepository).save(workspace);
        }

        @Test
        void shouldUpdateOnlyColor_whenNameIsNull() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            String originalName = workspace.getName();
            WorkspaceUpdateRequest request = new WorkspaceUpdateRequest(null, "#B2C3D4");
            WorkspaceUpdateResponse response = WorkspaceTestDataProvider.workspaceUpdateResponse(workspace);

            when(colorValidator.isColorMissing("#B2C3D4")).thenReturn(false);
            when(colorValidator.isHexColor("#B2C3D4")).thenReturn(true);
            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(workspaceRepository.save(workspace)).thenReturn(workspace);
            when(workspaceMapper.toWorkspaceUpdateResponse(workspace)).thenReturn(response);

            workspaceService.updateWorkspace(workspaceId, userId, request);

            assertThat(workspace.getName()).isEqualTo(originalName);
            assertThat(workspace.getColor()).isEqualTo("#B2C3D4");

            verify(colorValidator).isColorMissing("#B2C3D4");
            verify(colorValidator).isHexColor("#B2C3D4");
            verify(inputNormalizer, never()).normalize(any());
            verify(workspaceRepository).save(workspace);
        }

        @Test
        void shouldUpdateOnlyName_whenColorIsBlank() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            String originalColor = workspace.getColor();
            WorkspaceUpdateRequest request = new WorkspaceUpdateRequest("UpdatedName", "   ");
            WorkspaceUpdateResponse response = WorkspaceTestDataProvider.workspaceUpdateResponse(workspace);

            when(colorValidator.isColorMissing("   ")).thenReturn(true);
            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(inputNormalizer.normalize("UpdatedName")).thenReturn("UpdatedName");
            when(workspaceRepository.save(workspace)).thenReturn(workspace);
            when(workspaceMapper.toWorkspaceUpdateResponse(workspace)).thenReturn(response);

            workspaceService.updateWorkspace(workspaceId, userId, request);

            assertThat(workspace.getName()).isEqualTo("UpdatedName");
            assertThat(workspace.getColor()).isEqualTo(originalColor);

            verify(colorValidator).isColorMissing("   ");
            verify(colorValidator, never()).isHexColor(any());
            verify(inputNormalizer).normalize("UpdatedName");
            verify(workspaceRepository).save(workspace);
        }

        @Test
        void shouldUpdateOnlyColor_whenNameIsBlank() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            String originalName = workspace.getName();
            WorkspaceUpdateRequest request = new WorkspaceUpdateRequest("   ", "#B2C3D4");
            WorkspaceUpdateResponse response = WorkspaceTestDataProvider.workspaceUpdateResponse(workspace);

            when(colorValidator.isColorMissing("#B2C3D4")).thenReturn(false);
            when(colorValidator.isHexColor("#B2C3D4")).thenReturn(true);
            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(workspaceRepository.save(workspace)).thenReturn(workspace);
            when(workspaceMapper.toWorkspaceUpdateResponse(workspace)).thenReturn(response);

            workspaceService.updateWorkspace(workspaceId, userId, request);

            assertThat(workspace.getName()).isEqualTo(originalName);
            assertThat(workspace.getColor()).isEqualTo("#B2C3D4");

            verify(colorValidator).isColorMissing("#B2C3D4");
            verify(colorValidator).isHexColor("#B2C3D4");
            verify(inputNormalizer, never()).normalize(any());
            verify(workspaceRepository).save(workspace);
        }

        @Test
        void shouldThrowInvalidColorFormatException_whenColorFormatIsInvalid() {

            Long workspaceId = 1L;
            Long userId = 1L;

            WorkspaceUpdateRequest request = new WorkspaceUpdateRequest("UpdatedName", "rojo");

            when(colorValidator.isColorMissing("rojo")).thenReturn(false);
            when(colorValidator.isHexColor("rojo")).thenReturn(false);

            assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, userId, request))
                    .isInstanceOf(InvalidColorFormatException.class);

            verify(colorValidator).isColorMissing("rojo");
            verify(colorValidator).isHexColor("rojo");
            verify(workspaceAccessService, never()).loadWorkspaceWithOwnerAccess(any(), any());
            verify(workspaceRepository, never()).save(any());
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            WorkspaceUpdateRequest request = new WorkspaceUpdateRequest("UpdatedName", null);

            when(colorValidator.isColorMissing(null)).thenReturn(true);
            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, userId, request))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(workspaceRepository, never()).save(any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long userId = 2L;

            WorkspaceUpdateRequest request = new WorkspaceUpdateRequest("UpdatedName", null);

            when(colorValidator.isColorMissing(null)).thenReturn(true);
            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() -> workspaceService.updateWorkspace(workspaceId, userId, request))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(workspaceRepository, never()).save(any());
        }
    }

    @Nested
    class ArchiveWorkspaceTests {

        @Test
        void shouldArchiveWorkspace_whenUserIsOwner() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(workspaceRepository.save(workspace)).thenReturn(workspace);

            workspaceService.archiveWorkspace(workspaceId, userId);

            assertThat(workspace.getArchived()).isTrue();

            verify(workspaceAccessService).loadWorkspaceWithOwnerAccess(workspaceId, userId);
            verify(workspaceRepository).save(workspace);
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceService.archiveWorkspace(workspaceId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(workspaceRepository, never()).save(any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long userId = 2L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() -> workspaceService.archiveWorkspace(workspaceId, userId))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(workspaceRepository, never()).save(any());
        }
    }

    @Nested
    class UnarchiveWorkspaceTests {

        @Test
        void shouldUnarchiveWorkspace_whenUserIsOwner() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.archivedWorkspace(workspaceId);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(workspaceRepository.save(workspace)).thenReturn(workspace);

            workspaceService.unarchiveWorkspace(workspaceId, userId);

            assertThat(workspace.getArchived()).isFalse();

            verify(workspaceAccessService).loadWorkspaceWithOwnerAccess(workspaceId, userId);
            verify(workspaceRepository).save(workspace);
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceService.unarchiveWorkspace(workspaceId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(workspaceRepository, never()).save(any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long userId = 2L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() -> workspaceService.unarchiveWorkspace(workspaceId, userId))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(workspaceRepository, never()).save(any());
        }
    }

    @Nested
    class DeleteWorkspaceTests {

        @Test
        void shouldDeleteWorkspace_whenWorkspaceIsArchived() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.archivedWorkspace(workspaceId);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            doNothing().when(eventAccessService).removeAllWorkspaceEvents(workspaceId);
            doNothing().when(workspaceRepository).delete(workspace);

            workspaceService.deleteWorkspace(workspaceId, userId);

            verify(workspaceAccessService).loadWorkspaceWithOwnerAccess(workspaceId, userId);
            verify(eventAccessService).removeAllWorkspaceEvents(workspaceId);
            verify(workspaceRepository).delete(workspace);
        }

        @Test
        void shouldThrowWorkspaceNotArchivedException_whenWorkspaceIsNotArchived() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);

            assertThatThrownBy(() -> workspaceService.deleteWorkspace(workspaceId, userId))
                    .isInstanceOf(WorkspaceNotArchivedException.class);

            verify(eventAccessService, never()).removeAllWorkspaceEvents(any());
            verify(workspaceRepository, never()).delete(any());
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceService.deleteWorkspace(workspaceId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(eventAccessService, never()).removeAllWorkspaceEvents(any());
            verify(workspaceRepository, never()).delete(any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long userId = 2L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() -> workspaceService.deleteWorkspace(workspaceId, userId))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(eventAccessService, never()).removeAllWorkspaceEvents(any());
            verify(workspaceRepository, never()).delete(any());
        }
    }

    @Nested
    class GetInviteCodeTests {

        @Test
        void shouldReturnInviteCode_whenUserIsOwner() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);
            WorkspaceInviteCodeResponse response = WorkspaceTestDataProvider.workspaceInviteCodeResponse(inviteCode);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(workspaceInviteCodeLoader.loadByWorkspaceOrThrow(workspace)).thenReturn(inviteCode);
            when(workspaceInviteCodeSecurityService.decrypt(inviteCode.getInviteCodeEncrypted()))
                    .thenReturn(WorkspaceTestDataProvider.INVITE_CODE);
            when(workspaceInviteCodeMapper.toWorkspaceInviteCodeResponse(inviteCode, WorkspaceTestDataProvider.INVITE_CODE))
                    .thenReturn(response);

            WorkspaceInviteCodeResponse result = workspaceService.getInviteCode(workspaceId, userId);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(response);

            verify(workspaceAccessService).loadWorkspaceWithOwnerAccess(workspaceId, userId);
            verify(workspaceInviteCodeLoader).loadByWorkspaceOrThrow(workspace);
            verify(workspaceInviteCodeSecurityService).decrypt(inviteCode.getInviteCodeEncrypted());
            verify(workspaceInviteCodeMapper).toWorkspaceInviteCodeResponse(inviteCode, WorkspaceTestDataProvider.INVITE_CODE);
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceService.getInviteCode(workspaceId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(workspaceInviteCodeLoader, never()).loadByWorkspaceOrThrow(any());
            verify(workspaceInviteCodeSecurityService, never()).decrypt(any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long userId = 2L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() -> workspaceService.getInviteCode(workspaceId, userId))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(workspaceInviteCodeLoader, never()).loadByWorkspaceOrThrow(any());
            verify(workspaceInviteCodeSecurityService, never()).decrypt(any());
        }
    }

    @Nested
    class ActivateInviteCodeTests {

        @Test
        void shouldActivateInviteCode_whenUserIsOwner() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.disabledInviteCode(workspace);
            WorkspaceInviteCodeStatusResponse response = new WorkspaceInviteCodeStatusResponse(true);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(workspaceInviteCodeLoader.loadByWorkspaceOrThrow(workspace)).thenReturn(inviteCode);
            when(workspaceInviteCodeRepository.save(inviteCode)).thenReturn(inviteCode);
            when(workspaceInviteCodeMapper.toWorkspaceInviteCodeStatusResponse(inviteCode)).thenReturn(response);

            WorkspaceInviteCodeStatusResponse result = workspaceService.activateInviteCode(workspaceId, userId);

            assertThat(inviteCode.getInviteEnabled()).isTrue();
            assertThat(result.inviteEnabled()).isTrue();

            verify(workspaceInviteCodeRepository).save(inviteCode);
            verify(workspaceInviteCodeMapper).toWorkspaceInviteCodeStatusResponse(inviteCode);
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceService.activateInviteCode(workspaceId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(workspaceInviteCodeRepository, never()).save(any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long userId = 2L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() -> workspaceService.activateInviteCode(workspaceId, userId))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(workspaceInviteCodeRepository, never()).save(any());
        }
    }

    @Nested
    class DeactivateInviteCodeTests {

        @Test
        void shouldDeactivateInviteCode_whenUserIsOwner() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);
            WorkspaceInviteCodeStatusResponse response = new WorkspaceInviteCodeStatusResponse(false);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(workspaceInviteCodeLoader.loadByWorkspaceOrThrow(workspace)).thenReturn(inviteCode);
            when(workspaceInviteCodeRepository.save(inviteCode)).thenReturn(inviteCode);
            when(workspaceInviteCodeMapper.toWorkspaceInviteCodeStatusResponse(inviteCode)).thenReturn(response);

            WorkspaceInviteCodeStatusResponse result = workspaceService.deactivateInviteCode(workspaceId, userId);

            assertThat(inviteCode.getInviteEnabled()).isFalse();
            assertThat(result.inviteEnabled()).isFalse();

            verify(workspaceInviteCodeRepository).save(inviteCode);
            verify(workspaceInviteCodeMapper).toWorkspaceInviteCodeStatusResponse(inviteCode);
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceService.deactivateInviteCode(workspaceId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(workspaceInviteCodeRepository, never()).save(any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long userId = 2L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() -> workspaceService.deactivateInviteCode(workspaceId, userId))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(workspaceInviteCodeRepository, never()).save(any());
        }
    }

    @Nested
    class RegenerateInviteCodeTests {

        @Test
        void shouldRegenerateInviteCode_whenUserIsOwner() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);
            WorkspaceInviteCodeResponse response = WorkspaceTestDataProvider.workspaceInviteCodeResponse(inviteCode);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(workspaceInviteCodeLoader.loadByWorkspaceOrThrow(workspace)).thenReturn(inviteCode);
            when(workspaceInviteCodeGenerator.generate()).thenReturn(WorkspaceTestDataProvider.INVITE_CODE);
            when(workspaceInviteCodeSecurityService.encrypt(WorkspaceTestDataProvider.INVITE_CODE))
                    .thenReturn(WorkspaceTestDataProvider.INVITE_CODE_ENCRYPTED);
            when(workspaceInviteCodeRepository.save(inviteCode)).thenReturn(inviteCode);
            when(workspaceInviteCodeMapper.toWorkspaceInviteCodeResponse(eq(inviteCode), any()))
                    .thenReturn(response);

            WorkspaceInviteCodeResponse result = workspaceService.regenerateInviteCode(workspaceId, userId);

            assertThat(result).isNotNull();
            assertThat(inviteCode.getLastRegeneratedAt()).isNotNull();
            assertThat(inviteCode.getInviteCodeEncrypted()).isEqualTo(WorkspaceTestDataProvider.INVITE_CODE_ENCRYPTED);

            verify(workspaceInviteCodeGenerator).generate();
            verify(workspaceInviteCodeSecurityService).encrypt(WorkspaceTestDataProvider.INVITE_CODE);
            verify(workspaceInviteCodeRepository).save(inviteCode);
            verify(workspaceInviteCodeMapper).toWorkspaceInviteCodeResponse(eq(inviteCode), any());
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceService.regenerateInviteCode(workspaceId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(workspaceInviteCodeGenerator, never()).generate();
            verify(workspaceInviteCodeRepository, never()).save(any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long userId = 2L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() -> workspaceService.regenerateInviteCode(workspaceId, userId))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(workspaceInviteCodeGenerator, never()).generate();
            verify(workspaceInviteCodeRepository, never()).save(any());
        }
    }

    @Nested
    class JoinWorkspaceTests {

        @Test
        void shouldJoinWorkspace_whenValidInviteCode() {

            Long userId = 2L;

            User user = UserTestDataProvider.user(userId);
            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);
            WorkspaceUser workspaceUser = WorkspaceTestDataProvider.memberWorkspaceUser(workspace, user);
            WorkspaceJoinResponse response = WorkspaceTestDataProvider.workspaceJoinResponse(workspaceUser);

            when(workspaceInviteCodeLoader.loadEnabledByCodeOrThrow(WorkspaceTestDataProvider.INVITE_CODE))
                    .thenReturn(inviteCode);
            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);
            when(workspaceUserRepository.existsByWorkspaceIdAndUserId(workspace.getId(), userId)).thenReturn(false);
            when(workspaceConstraintsProperties.getMaxUsers()).thenReturn(10);
            when(workspaceUserRepository.countByWorkspaceId(workspace.getId())).thenReturn(1L);
            when(workspaceUserRepository.save(any(WorkspaceUser.class))).thenReturn(workspaceUser);
            when(workspaceUserMapper.toWorkspaceJoinResponse(any(WorkspaceUser.class))).thenReturn(response);

            WorkspaceJoinResponse result = workspaceService.joinWorkspace(WorkspaceTestDataProvider.INVITE_CODE, userId);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(response);

            verify(workspaceInviteCodeLoader).loadEnabledByCodeOrThrow(WorkspaceTestDataProvider.INVITE_CODE);
            verify(userLoader).loadUserOrThrow(userId);
            verify(workspaceUserRepository).save(any(WorkspaceUser.class));
            verify(eventAccessService).assignUserToGlobalEvents(workspace.getId(), userId);
        }

        @Test
        void shouldThrowUserAlreadyInWorkspaceException_whenUserAlreadyInWorkspace() {

            Long userId = 2L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);

            when(workspaceInviteCodeLoader.loadEnabledByCodeOrThrow(WorkspaceTestDataProvider.INVITE_CODE))
                    .thenReturn(inviteCode);
            when(userLoader.loadUserOrThrow(userId)).thenReturn(UserTestDataProvider.user(userId));
            when(workspaceUserRepository.existsByWorkspaceIdAndUserId(workspace.getId(), userId)).thenReturn(true);

            assertThatThrownBy(() -> workspaceService.joinWorkspace(WorkspaceTestDataProvider.INVITE_CODE, userId))
                    .isInstanceOf(UserAlreadyInWorkspaceException.class);

            verify(workspaceUserRepository, never()).save(any());
            verify(eventAccessService, never()).assignUserToGlobalEvents(any(), any());
        }

        @Test
        void shouldThrowWorkspaceCapacityExceededException_whenWorkspaceIsFull() {

            Long userId = 2L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);

            when(workspaceInviteCodeLoader.loadEnabledByCodeOrThrow(WorkspaceTestDataProvider.INVITE_CODE))
                    .thenReturn(inviteCode);
            when(userLoader.loadUserOrThrow(userId)).thenReturn(UserTestDataProvider.user(userId));
            when(workspaceUserRepository.existsByWorkspaceIdAndUserId(workspace.getId(), userId)).thenReturn(false);
            when(workspaceConstraintsProperties.getMaxUsers()).thenReturn(10);
            when(workspaceUserRepository.countByWorkspaceId(workspace.getId())).thenReturn(10L);

            assertThatThrownBy(() -> workspaceService.joinWorkspace(WorkspaceTestDataProvider.INVITE_CODE, userId))
                    .isInstanceOf(WorkspaceCapacityExceededException.class);

            verify(workspaceUserRepository, never()).save(any());
            verify(eventAccessService, never()).assignUserToGlobalEvents(any(), any());
        }

        @Test
        void shouldThrowInvalidWorkspaceInviteCodeException_whenInviteCodeIsInvalid() {

            Long userId = 2L;

            when(workspaceInviteCodeLoader.loadEnabledByCodeOrThrow("INVALIDCODE0"))
                    .thenThrow(new InvalidWorkspaceInviteCodeException());

            assertThatThrownBy(() -> workspaceService.joinWorkspace("INVALIDCODE0", userId))
                    .isInstanceOf(InvalidWorkspaceInviteCodeException.class);

            verify(userLoader, never()).loadUserOrThrow(any());
            verify(workspaceUserRepository, never()).save(any());
        }

        @Test
        void shouldThrowWorkspaceInviteCodeDisabledException_whenInviteCodeIsDisabled() {

            Long userId = 2L;

            when(workspaceInviteCodeLoader.loadEnabledByCodeOrThrow(WorkspaceTestDataProvider.INVITE_CODE))
                    .thenThrow(new WorkspaceInviteCodeDisabledException());

            assertThatThrownBy(() -> workspaceService.joinWorkspace(WorkspaceTestDataProvider.INVITE_CODE, userId))
                    .isInstanceOf(WorkspaceInviteCodeDisabledException.class);

            verify(userLoader, never()).loadUserOrThrow(any());
            verify(workspaceUserRepository, never()).save(any());
        }
    }

    @Nested
    class GetWorkspaceUsersTests {

        @Test
        void shouldReturnPagedUsers_whenUserHasAccess() {

            Long workspaceId = 1L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            WorkspaceUser workspaceUser = WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, user);
            WorkspaceUserResponse userResponse = new WorkspaceUserResponse(
                    userId, user.getFirstName(), user.getLastName(), WorkspaceRole.OWNER
            );

            Page<WorkspaceUser> page = new PageImpl<>(List.of(workspaceUser));

            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId)).thenReturn(workspaceUser);
            when(workspaceUserRepository.findByWorkspaceId(workspaceId, pageable)).thenReturn(page);
            when(workspaceUserMapper.toWorkspaceUserResponse(workspaceUser)).thenReturn(userResponse);

            PageResponse<WorkspaceUserResponse> result =
                    workspaceService.getWorkspaceUsers(workspaceId, userId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.content()).hasSize(1);
            assertThat(result.content().get(0)).usingRecursiveComparison().isEqualTo(userResponse);

            verify(workspaceAccessService).requireAccessibleWorkspace(workspaceId, userId);
            verify(workspaceUserRepository).findByWorkspaceId(workspaceId, pageable);
            verify(workspaceUserMapper).toWorkspaceUserResponse(workspaceUser);
        }

        @Test
        void shouldReturnEmptyPage_whenWorkspaceHasNoUsers() {

            Long workspaceId = 1L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            WorkspaceUser workspaceUser = WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, user);

            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId)).thenReturn(workspaceUser);
            when(workspaceUserRepository.findByWorkspaceId(workspaceId, pageable)).thenReturn(Page.empty());

            PageResponse<WorkspaceUserResponse> result =
                    workspaceService.getWorkspaceUsers(workspaceId, userId, pageable);

            assertThat(result.content()).isEmpty();

            verify(workspaceUserMapper, never()).toWorkspaceUserResponse(any());
        }

        @Test
        void shouldThrowWorkspaceAccessDeniedException_whenUserHasNoAccess() {

            Long workspaceId = 1L;
            Long userId = 99L;
            Pageable pageable = Pageable.unpaged();

            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenThrow(new WorkspaceAccessDeniedException());

            assertThatThrownBy(() -> workspaceService.getWorkspaceUsers(workspaceId, userId, pageable))
                    .isInstanceOf(WorkspaceAccessDeniedException.class);

            verify(workspaceUserRepository, never()).findByWorkspaceId(any(), any());
            verify(workspaceUserMapper, never()).toWorkspaceUserResponse(any());
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;
            Pageable pageable = Pageable.unpaged();

            when(paginationValidator.validate(pageable)).thenReturn(pageable);
            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceService.getWorkspaceUsers(workspaceId, userId, pageable))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(workspaceUserRepository, never()).findByWorkspaceId(any(), any());
            verify(workspaceUserMapper, never()).toWorkspaceUserResponse(any());
        }
    }

    @Nested
    class RemoveWorkspaceUserTests {

        @Test
        void shouldRemoveUser_whenUserIsOwnerAndTargetIsMember() {

            Long workspaceId = 1L;
            Long userId = 1L;
            Long targetUserId = 2L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User targetUser = UserTestDataProvider.user(targetUserId);
            WorkspaceUser targetWorkspaceUser = WorkspaceTestDataProvider.memberWorkspaceUser(workspace, targetUser);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(workspaceUserRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId))
                    .thenReturn(Optional.of(targetWorkspaceUser));
            doNothing().when(eventAccessService).removeUserFromWorkspaceEvents(workspaceId, targetUserId);
            doNothing().when(workspaceUserRepository).delete(targetWorkspaceUser);

            workspaceService.removeWorkspaceUser(workspaceId, targetUserId, userId);

            verify(workspaceAccessService).loadWorkspaceWithOwnerAccess(workspaceId, userId);
            verify(workspaceUserRepository).findByWorkspaceIdAndUserId(workspaceId, targetUserId);
            verify(eventAccessService).removeUserFromWorkspaceEvents(workspaceId, targetUserId);
            verify(workspaceUserRepository).delete(targetWorkspaceUser);
        }

        @Test
        void shouldThrowWorkspaceOperationNotAllowedException_whenTargetIsOwner() {

            Long workspaceId = 1L;
            Long userId = 1L;
            Long targetUserId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User targetUser = UserTestDataProvider.user(targetUserId);
            WorkspaceUser targetWorkspaceUser = WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, targetUser);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(workspaceUserRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId))
                    .thenReturn(Optional.of(targetWorkspaceUser));

            assertThatThrownBy(() -> workspaceService.removeWorkspaceUser(workspaceId, targetUserId, userId))
                    .isInstanceOf(WorkspaceOperationNotAllowedException.class);

            verify(eventAccessService, never()).removeUserFromWorkspaceEvents(any(), any());
            verify(workspaceUserRepository, never()).delete(any());
        }

        @Test
        void shouldThrowWorkspaceUserNotFoundException_whenTargetUserIsNotInWorkspace() {

            Long workspaceId = 1L;
            Long userId = 1L;
            Long targetUserId = 99L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId)).thenReturn(workspace);
            when(workspaceUserRepository.findByWorkspaceIdAndUserId(workspaceId, targetUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceService.removeWorkspaceUser(workspaceId, targetUserId, userId))
                    .isInstanceOf(WorkspaceUserNotFoundException.class);

            verify(eventAccessService, never()).removeUserFromWorkspaceEvents(any(), any());
            verify(workspaceUserRepository, never()).delete(any());
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long userId = 2L;
            Long targetUserId = 3L;

            when(workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .thenThrow(new WorkspaceRoleDeniedException());

            assertThatThrownBy(() -> workspaceService.removeWorkspaceUser(workspaceId, targetUserId, userId))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(workspaceUserRepository, never()).findByWorkspaceIdAndUserId(any(), any());
            verify(eventAccessService, never()).removeUserFromWorkspaceEvents(any(), any());
            verify(workspaceUserRepository, never()).delete(any());
        }
    }

    @Nested
    class LeaveWorkspaceTests {

        @Test
        void shouldLeaveWorkspace_whenUserIsMember() {

            Long workspaceId = 1L;
            Long userId = 2L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            WorkspaceUser workspaceUser = WorkspaceTestDataProvider.memberWorkspaceUser(workspace, user);

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId)).thenReturn(workspaceUser);
            doNothing().when(eventAccessService).removeUserFromWorkspaceEvents(workspaceId, userId);
            doNothing().when(workspaceUserRepository).delete(workspaceUser);

            workspaceService.leaveWorkspace(workspaceId, userId);

            verify(workspaceAccessService).requireAccessibleWorkspace(workspaceId, userId);
            verify(eventAccessService).removeUserFromWorkspaceEvents(workspaceId, userId);
            verify(workspaceUserRepository).delete(workspaceUser);
        }

        @Test
        void shouldThrowWorkspaceOperationNotAllowedException_whenUserIsOwner() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            WorkspaceUser workspaceUser = WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, user);

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId)).thenReturn(workspaceUser);

            assertThatThrownBy(() -> workspaceService.leaveWorkspace(workspaceId, userId))
                    .isInstanceOf(WorkspaceOperationNotAllowedException.class);

            verify(eventAccessService, never()).removeUserFromWorkspaceEvents(any(), any());
            verify(workspaceUserRepository, never()).delete(any());
        }

        @Test
        void shouldThrowWorkspaceAccessDeniedException_whenUserIsNotInWorkspace() {

            Long workspaceId = 1L;
            Long userId = 99L;

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenThrow(new WorkspaceAccessDeniedException());

            assertThatThrownBy(() -> workspaceService.leaveWorkspace(workspaceId, userId))
                    .isInstanceOf(WorkspaceAccessDeniedException.class);

            verify(eventAccessService, never()).removeUserFromWorkspaceEvents(any(), any());
            verify(workspaceUserRepository, never()).delete(any());
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            when(workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceService.leaveWorkspace(workspaceId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(eventAccessService, never()).removeUserFromWorkspaceEvents(any(), any());
            verify(workspaceUserRepository, never()).delete(any());
        }
    }
}