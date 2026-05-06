package com.tempertime.tempertime_api.workspaces.service.authorization;

import com.tempertime.tempertime_api.users.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.workspaces.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceUser;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceAccessDeniedException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceNotFoundException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceOwnerExistsException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceRoleDeniedException;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceUserRepository;
import com.tempertime.tempertime_api.workspaces.service.loader.WorkspaceLoader;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkspaceAccessServiceTest {

    @Mock
    private WorkspaceUserRepository workspaceUserRepository;

    @Mock
    private WorkspaceLoader workspaceLoader;

    @Mock
    private WorkspaceAuthorizationService authorizationService;

    @InjectMocks
    private WorkspaceAccessService workspaceAccessService;

    @Nested
    class RequireAccessibleWorkspaceTests {

        @Test
        void shouldReturnWorkspaceUser_whenWorkspaceExistsAndUserIsInWorkspace() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            WorkspaceUser workspaceUser = WorkspaceTestDataProvider.memberWorkspaceUser(workspace, user);

            when(workspaceLoader.loadOrThrow(workspaceId)).thenReturn(workspace);
            when(authorizationService.requireMembership(workspaceId, userId)).thenReturn(workspaceUser);

            WorkspaceUser result = workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(workspaceUser);

            verify(workspaceLoader).loadOrThrow(workspaceId);
            verify(authorizationService).requireMembership(workspaceId, userId);
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            when(workspaceLoader.loadOrThrow(workspaceId)).thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(workspaceLoader).loadOrThrow(workspaceId);
            verify(authorizationService, never()).requireMembership(any(), any());
        }

        @Test
        void shouldThrowWorkspaceAccessDeniedException_whenUserIsNotInWorkspace() {

            Long workspaceId = 1L;
            Long userId = 99L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceLoader.loadOrThrow(workspaceId)).thenReturn(workspace);
            when(authorizationService.requireMembership(workspaceId, userId))
                    .thenThrow(new WorkspaceAccessDeniedException());

            assertThatThrownBy(() -> workspaceAccessService.requireAccessibleWorkspace(workspaceId, userId))
                    .isInstanceOf(WorkspaceAccessDeniedException.class);

            verify(workspaceLoader).loadOrThrow(workspaceId);
            verify(authorizationService).requireMembership(workspaceId, userId);
        }
    }

    @Nested
    class LoadWorkspaceWithOwnerAccessTests {

        @Test
        void shouldReturnWorkspace_whenUserIsOwner() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceLoader.loadOrThrow(workspaceId)).thenReturn(workspace);
            doNothing().when(authorizationService).requireRole(workspaceId, userId, WorkspaceRole.OWNER);

            Workspace result = workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(workspace);

            verify(workspaceLoader).loadOrThrow(workspaceId);
            verify(authorizationService).requireRole(workspaceId, userId, WorkspaceRole.OWNER);
        }

        @Test
        void shouldThrowWorkspaceNotFoundException_whenWorkspaceDoesNotExist() {

            Long workspaceId = 99L;
            Long userId = 1L;

            when(workspaceLoader.loadOrThrow(workspaceId)).thenThrow(new WorkspaceNotFoundException());

            assertThatThrownBy(() -> workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .isInstanceOf(WorkspaceNotFoundException.class);

            verify(workspaceLoader).loadOrThrow(workspaceId);
            verify(authorizationService, never()).requireRole(any(), any(), any());
        }

        @Test
        void shouldThrowWorkspaceAccessDeniedException_whenUserIsNotInWorkspace() {

            Long workspaceId = 1L;
            Long userId = 99L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceLoader.loadOrThrow(workspaceId)).thenReturn(workspace);
            doThrow(new WorkspaceAccessDeniedException())
                    .when(authorizationService).requireRole(workspaceId, userId, WorkspaceRole.OWNER);

            assertThatThrownBy(() -> workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .isInstanceOf(WorkspaceAccessDeniedException.class);

            verify(workspaceLoader).loadOrThrow(workspaceId);
            verify(authorizationService).requireRole(workspaceId, userId, WorkspaceRole.OWNER);
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserIsNotOwner() {

            Long workspaceId = 1L;
            Long userId = 2L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceLoader.loadOrThrow(workspaceId)).thenReturn(workspace);
            doThrow(new WorkspaceRoleDeniedException())
                    .when(authorizationService).requireRole(workspaceId, userId, WorkspaceRole.OWNER);

            assertThatThrownBy(() -> workspaceAccessService.loadWorkspaceWithOwnerAccess(workspaceId, userId))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(workspaceLoader).loadOrThrow(workspaceId);
            verify(authorizationService).requireRole(workspaceId, userId, WorkspaceRole.OWNER);
        }
    }

    @Nested
    class RequireNoOwnedWorkspacesTests {

        @Test
        void shouldNotThrow_whenUserHasNoOwnedWorkspaces() {

            Long userId = 1L;

            when(workspaceUserRepository.existsByUserIdAndRole(userId, WorkspaceRole.OWNER)).thenReturn(false);

            assertThatCode(() -> workspaceAccessService.requireNoOwnedWorkspaces(userId))
                    .doesNotThrowAnyException();

            verify(workspaceUserRepository).existsByUserIdAndRole(userId, WorkspaceRole.OWNER);
        }

        @Test
        void shouldThrowWorkspaceOwnerExistsException_whenUserOwnsWorkspaces() {

            Long userId = 1L;

            when(workspaceUserRepository.existsByUserIdAndRole(userId, WorkspaceRole.OWNER)).thenReturn(true);

            assertThatThrownBy(() -> workspaceAccessService.requireNoOwnedWorkspaces(userId))
                    .isInstanceOf(WorkspaceOwnerExistsException.class);

            verify(workspaceUserRepository).existsByUserIdAndRole(userId, WorkspaceRole.OWNER);
        }
    }
}