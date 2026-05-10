package com.tempertime.tempertime_api.workspaces.repository;

import com.tempertime.tempertime_api.workspaces.data.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceInviteCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Transactional
public class WorkspaceInviteCodeRepositoryTest {

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceInviteCodeRepository workspaceInviteCodeRepository;

    private Workspace savedWorkspace;

    @BeforeEach
    void setUp() {
        savedWorkspace = workspaceRepository.save(WorkspaceTestDataProvider.workspace(null));
    }

    @Nested
    class FindByWorkspaceIdTests {

        @Test
        void shouldReturnInviteCode_whenWorkspaceHasInviteCode() {

            workspaceInviteCodeRepository.save(
                    WorkspaceTestDataProvider.inviteCode(savedWorkspace)
            );

            Optional<WorkspaceInviteCode> result = workspaceInviteCodeRepository
                    .findByWorkspaceId(savedWorkspace.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getWorkspace().getId()).isEqualTo(savedWorkspace.getId());
        }

        @Test
        void shouldReturnEmpty_whenWorkspaceHasNoInviteCode() {

            Optional<WorkspaceInviteCode> result = workspaceInviteCodeRepository
                    .findByWorkspaceId(savedWorkspace.getId());

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnOnlyInviteCodeForGivenWorkspace_whenMultipleWorkspacesExist() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );

            workspaceInviteCodeRepository.save(
                    WorkspaceTestDataProvider.inviteCode(savedWorkspace)
            );
            workspaceInviteCodeRepository.save(
                    WorkspaceTestDataProvider.inviteCode(
                            otherWorkspace, "otherEncrypted", "otherHash"
                    )
            );

            Optional<WorkspaceInviteCode> result = workspaceInviteCodeRepository
                    .findByWorkspaceId(savedWorkspace.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getWorkspace().getId()).isEqualTo(savedWorkspace.getId());
        }
    }

    @Nested
    class FindByInviteCodeHashTests {

        @Test
        void shouldReturnInviteCode_whenHashExists() {

            workspaceInviteCodeRepository.save(
                    WorkspaceTestDataProvider.inviteCode(savedWorkspace)
            );

            Optional<WorkspaceInviteCode> result = workspaceInviteCodeRepository
                    .findByInviteCodeHash(WorkspaceTestDataProvider.INVITE_CODE_HASH);

            assertThat(result).isPresent();
            assertThat(result.get().getInviteCodeHash())
                    .isEqualTo(WorkspaceTestDataProvider.INVITE_CODE_HASH);
        }

        @Test
        void shouldReturnEmpty_whenHashDoesNotExist() {

            Optional<WorkspaceInviteCode> result = workspaceInviteCodeRepository
                    .findByInviteCodeHash("nonexistent");

            assertThat(result).isEmpty();
        }
    }
}
