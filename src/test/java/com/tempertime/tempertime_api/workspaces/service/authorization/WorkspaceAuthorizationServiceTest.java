package com.tempertime.tempertime_api.workspaces.service.authorization;

import com.tempertime.tempertime_api.users.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.workspaces.data.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceUser;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceAccessDeniedException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceRoleDeniedException;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceUserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WorkspaceAuthorizationServiceTest {

    @Mock
    private WorkspaceUserRepository workspaceUserRepository;

    @InjectMocks
    private WorkspaceAuthorizationService workspaceAuthorizationService;

    @Nested
    class RequireMembershipTests {

        @Test
        void shouldReturnWorkspaceUser_whenUserIsInWorkspace() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            WorkspaceUser workspaceUser = WorkspaceTestDataProvider.memberWorkspaceUser(workspace, user);

            when(workspaceUserRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                    .thenReturn(Optional.of(workspaceUser));

            WorkspaceUser result = workspaceAuthorizationService.requireMembership(workspaceId, userId);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(workspaceUser);

            verify(workspaceUserRepository).findByWorkspaceIdAndUserId(workspaceId, userId);
        }

        @Test
        void shouldThrowWorkspaceAccessDeniedException_whenUserIsNotInWorkspace() {

            Long workspaceId = 1L;
            Long userId = 99L;

            when(workspaceUserRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceAuthorizationService.requireMembership(workspaceId, userId))
                    .isInstanceOf(WorkspaceAccessDeniedException.class);

            verify(workspaceUserRepository).findByWorkspaceIdAndUserId(workspaceId, userId);
        }
    }

    @Nested
    class RequireRoleTests {

        @Test
        void shouldNotThrow_whenUserHasRequiredRole() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            WorkspaceUser workspaceUser = WorkspaceTestDataProvider.ownerWorkspaceUser(workspace, user);

            when(workspaceUserRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                    .thenReturn(Optional.of(workspaceUser));

            assertThatCode(() -> workspaceAuthorizationService.requireRole(workspaceId, userId, WorkspaceRole.OWNER))
                    .doesNotThrowAnyException();

            verify(workspaceUserRepository).findByWorkspaceIdAndUserId(workspaceId, userId);
        }

        @Test
        void shouldThrowWorkspaceRoleDeniedException_whenUserHasWrongRole() {

            Long workspaceId = 1L;
            Long userId = 2L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            WorkspaceUser workspaceUser = WorkspaceTestDataProvider.memberWorkspaceUser(workspace, user);

            when(workspaceUserRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                    .thenReturn(Optional.of(workspaceUser));

            assertThatThrownBy(() -> workspaceAuthorizationService.requireRole(workspaceId, userId, WorkspaceRole.OWNER))
                    .isInstanceOf(WorkspaceRoleDeniedException.class);

            verify(workspaceUserRepository).findByWorkspaceIdAndUserId(workspaceId, userId);
        }

        @Test
        void shouldThrowWorkspaceAccessDeniedException_whenUserIsNotInWorkspace() {

            Long workspaceId = 1L;
            Long userId = 99L;

            when(workspaceUserRepository.findByWorkspaceIdAndUserId(workspaceId, userId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceAuthorizationService.requireRole(workspaceId, userId, WorkspaceRole.OWNER))
                    .isInstanceOf(WorkspaceAccessDeniedException.class);

            verify(workspaceUserRepository).findByWorkspaceIdAndUserId(workspaceId, userId);
        }
    }
}
