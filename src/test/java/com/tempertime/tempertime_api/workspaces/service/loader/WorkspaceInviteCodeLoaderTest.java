package com.tempertime.tempertime_api.workspaces.service.loader;

import com.tempertime.tempertime_api.common.hash.Hash;
import com.tempertime.tempertime_api.workspaces.data.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceInviteCode;
import com.tempertime.tempertime_api.workspaces.exception.InvalidWorkspaceInviteCodeException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceInviteCodeDisabledException;
import com.tempertime.tempertime_api.workspaces.exception.WorkspaceInviteCodeNotFoundException;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceInviteCodeRepository;
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
public class WorkspaceInviteCodeLoaderTest {

    @Mock
    private WorkspaceInviteCodeRepository workspaceInviteCodeRepository;

    @InjectMocks
    private WorkspaceInviteCodeLoader workspaceInviteCodeLoader;

    @Nested
    class LoadByWorkspaceOrThrowTests {

        @Test
        void shouldReturnInviteCode_whenWorkspaceHasInviteCode() {

            Long workspaceId = 1L;
            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);

            when(workspaceInviteCodeRepository.findByWorkspaceId(workspaceId))
                    .thenReturn(Optional.of(inviteCode));

            WorkspaceInviteCode result = workspaceInviteCodeLoader.loadByWorkspaceOrThrow(workspace);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(inviteCode);

            verify(workspaceInviteCodeRepository).findByWorkspaceId(workspaceId);
        }

        @Test
        void shouldThrowWorkspaceInviteCodeNotFoundException_whenWorkspaceHasNoInviteCode() {

            Long workspaceId = 99L;
            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);

            when(workspaceInviteCodeRepository.findByWorkspaceId(workspaceId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceInviteCodeLoader.loadByWorkspaceOrThrow(workspace))
                    .isInstanceOf(WorkspaceInviteCodeNotFoundException.class);

            verify(workspaceInviteCodeRepository).findByWorkspaceId(workspaceId);
        }
    }

    @Nested
    class LoadByWorkspaceIdOrThrowTests {

        @Test
        void shouldReturnInviteCode_whenWorkspaceHasInviteCode() {

            Long workspaceId = 1L;
            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);

            when(workspaceInviteCodeRepository.findByWorkspaceId(workspaceId))
                    .thenReturn(Optional.of(inviteCode));

            WorkspaceInviteCode result = workspaceInviteCodeLoader.loadByWorkspaceIdOrThrow(workspaceId);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(inviteCode);

            verify(workspaceInviteCodeRepository).findByWorkspaceId(workspaceId);
        }

        @Test
        void shouldThrowWorkspaceInviteCodeNotFoundException_whenWorkspaceHasNoInviteCode() {

            Long workspaceId = 99L;

            when(workspaceInviteCodeRepository.findByWorkspaceId(workspaceId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceInviteCodeLoader.loadByWorkspaceIdOrThrow(workspaceId))
                    .isInstanceOf(WorkspaceInviteCodeNotFoundException.class);

            verify(workspaceInviteCodeRepository).findByWorkspaceId(workspaceId);
        }
    }

    @Nested
    class LoadEnabledByCodeOrThrowTests {

        @Test
        void shouldReturnInviteCode_whenCodeIsValidAndEnabled() {

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);

            String normalizedCode = WorkspaceTestDataProvider.INVITE_CODE.trim().toUpperCase();
            String hashedCode = Hash.sha256(normalizedCode);

            when(workspaceInviteCodeRepository.findByInviteCodeHash(hashedCode))
                    .thenReturn(Optional.of(inviteCode));

            WorkspaceInviteCode result =
                    workspaceInviteCodeLoader.loadEnabledByCodeOrThrow(WorkspaceTestDataProvider.INVITE_CODE);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(inviteCode);

            verify(workspaceInviteCodeRepository).findByInviteCodeHash(hashedCode);
        }

        @Test
        void shouldThrowWorkspaceInviteCodeDisabledException_whenCodeIsDisabled() {

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.disabledInviteCode(workspace);

            String normalizedCode = WorkspaceTestDataProvider.INVITE_CODE.trim().toUpperCase();
            String hashedCode = Hash.sha256(normalizedCode);

            when(workspaceInviteCodeRepository.findByInviteCodeHash(hashedCode))
                    .thenReturn(Optional.of(inviteCode));

            assertThatThrownBy(() ->
                    workspaceInviteCodeLoader.loadEnabledByCodeOrThrow(WorkspaceTestDataProvider.INVITE_CODE))
                    .isInstanceOf(WorkspaceInviteCodeDisabledException.class);

            verify(workspaceInviteCodeRepository).findByInviteCodeHash(hashedCode);
        }

        @Test
        void shouldThrowInvalidWorkspaceInviteCodeException_whenCodeDoesNotExist() {

            String rawCode = "INVALIDCODE0";
            String normalizedCode = rawCode.trim().toUpperCase();
            String hashedCode = Hash.sha256(normalizedCode);

            when(workspaceInviteCodeRepository.findByInviteCodeHash(hashedCode))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceInviteCodeLoader.loadEnabledByCodeOrThrow(rawCode))
                    .isInstanceOf(InvalidWorkspaceInviteCodeException.class);

            verify(workspaceInviteCodeRepository).findByInviteCodeHash(hashedCode);
        }
    }

    @Nested
    class LoadByCodeOrThrowTests {

        @Test
        void shouldReturnInviteCode_whenCodeExists() {

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);

            String normalizedCode = WorkspaceTestDataProvider.INVITE_CODE.trim().toUpperCase();
            String hashedCode = Hash.sha256(normalizedCode);

            when(workspaceInviteCodeRepository.findByInviteCodeHash(hashedCode))
                    .thenReturn(Optional.of(inviteCode));

            WorkspaceInviteCode result =
                    workspaceInviteCodeLoader.loadByCodeOrThrow(WorkspaceTestDataProvider.INVITE_CODE);

            assertThat(result).isNotNull();
            assertThat(result).usingRecursiveComparison().isEqualTo(inviteCode);

            verify(workspaceInviteCodeRepository).findByInviteCodeHash(hashedCode);
        }

        @Test
        void shouldNormalizeCodeBeforeLookup_whenCodeHasWhitespaceAndLowercase() {

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            WorkspaceInviteCode inviteCode = WorkspaceTestDataProvider.inviteCode(workspace);

            String rawCode = "  testcode123  ";
            String normalizedCode = rawCode.trim().toUpperCase();
            String hashedCode = Hash.sha256(normalizedCode);

            when(workspaceInviteCodeRepository.findByInviteCodeHash(hashedCode))
                    .thenReturn(Optional.of(inviteCode));

            WorkspaceInviteCode result = workspaceInviteCodeLoader.loadByCodeOrThrow(rawCode);

            assertThat(result).isNotNull();
            verify(workspaceInviteCodeRepository).findByInviteCodeHash(hashedCode);
        }

        @Test
        void shouldThrowInvalidWorkspaceInviteCodeException_whenCodeDoesNotExist() {

            String rawCode = "INVALIDCODE0";
            String normalizedCode = rawCode.trim().toUpperCase();
            String hashedCode = Hash.sha256(normalizedCode);

            when(workspaceInviteCodeRepository.findByInviteCodeHash(hashedCode))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> workspaceInviteCodeLoader.loadByCodeOrThrow(rawCode))
                    .isInstanceOf(InvalidWorkspaceInviteCodeException.class);

            verify(workspaceInviteCodeRepository).findByInviteCodeHash(hashedCode);
        }
    }
}
