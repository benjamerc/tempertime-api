package com.tempertime.tempertime_api.workspaces.repository;

import com.tempertime.tempertime_api.users.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import com.tempertime.tempertime_api.workspaces.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceRole;
import com.tempertime.tempertime_api.workspaces.domain.WorkspaceUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Transactional
public class WorkspaceUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private WorkspaceUserRepository workspaceUserRepository;

    private User savedUser;
    private Workspace savedWorkspace;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(UserTestDataProvider.user(null));
        savedWorkspace = workspaceRepository.save(WorkspaceTestDataProvider.workspace(null));
    }

    @Nested
    class FindByWorkspaceIdAndUserIdTests {

        @Test
        void shouldReturnWorkspaceUser_whenMembershipExists() {

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(savedWorkspace, savedUser)
            );

            Optional<WorkspaceUser> result = workspaceUserRepository
                    .findByWorkspaceIdAndUserId(savedWorkspace.getId(), savedUser.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getWorkspace().getId()).isEqualTo(savedWorkspace.getId());
            assertThat(result.get().getUser().getId()).isEqualTo(savedUser.getId());
        }

        @Test
        void shouldReturnEmpty_whenMembershipDoesNotExist() {

            Optional<WorkspaceUser> result = workspaceUserRepository
                    .findByWorkspaceIdAndUserId(savedWorkspace.getId(), savedUser.getId());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class ExistsByWorkspaceIdAndUserIdTests {

        @Test
        void shouldReturnTrue_whenMembershipExists() {

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(savedWorkspace, savedUser)
            );

            boolean exists = workspaceUserRepository
                    .existsByWorkspaceIdAndUserId(savedWorkspace.getId(), savedUser.getId());

            assertThat(exists).isTrue();
        }

        @Test
        void shouldReturnFalse_whenMembershipDoesNotExist() {

            boolean exists = workspaceUserRepository
                    .existsByWorkspaceIdAndUserId(savedWorkspace.getId(), savedUser.getId());

            assertThat(exists).isFalse();
        }
    }

    @Nested
    class FindByWorkspaceIdTests {

        @Test
        void shouldReturnPagedUsers_whenWorkspaceHasUsers() {

            User secondUser = userRepository.save(UserTestDataProvider.user(null));

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(savedWorkspace, savedUser)
            );
            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(savedWorkspace, secondUser)
            );

            Page<WorkspaceUser> result = workspaceUserRepository
                    .findByWorkspaceId(savedWorkspace.getId(), Pageable.unpaged());

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(wu -> wu.getUser().getId())
                    .containsExactlyInAnyOrder(savedUser.getId(), secondUser.getId());
        }

        @Test
        void shouldReturnEmptyPage_whenWorkspaceHasNoUsers() {

            Page<WorkspaceUser> result = workspaceUserRepository
                    .findByWorkspaceId(savedWorkspace.getId(), Pageable.unpaged());

            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        void shouldReturnOnlyUsersFromGivenWorkspace_whenMultipleWorkspacesExist() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );
            User otherUser = userRepository.save(UserTestDataProvider.user(null));

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(savedWorkspace, savedUser)
            );
            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(otherWorkspace, otherUser)
            );

            Page<WorkspaceUser> result = workspaceUserRepository
                    .findByWorkspaceId(savedWorkspace.getId(), Pageable.unpaged());

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(savedUser.getId());
        }
    }

    @Nested
    class CountByWorkspaceIdTests {

        @Test
        void shouldReturnCorrectCount_whenWorkspaceHasUsers() {

            User secondUser = userRepository.save(UserTestDataProvider.user(null));

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(savedWorkspace, savedUser)
            );
            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(savedWorkspace, secondUser)
            );

            long count = workspaceUserRepository.countByWorkspaceId(savedWorkspace.getId());

            assertThat(count).isEqualTo(2);
        }

        @Test
        void shouldReturnZero_whenWorkspaceHasNoUsers() {

            long count = workspaceUserRepository.countByWorkspaceId(savedWorkspace.getId());

            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    class ExistsByUserIdAndRoleTests {

        @Test
        void shouldReturnTrue_whenUserHasOwnerRole() {

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(savedWorkspace, savedUser)
            );

            boolean exists = workspaceUserRepository
                    .existsByUserIdAndRole(savedUser.getId(), WorkspaceRole.OWNER);

            assertThat(exists).isTrue();
        }

        @Test
        void shouldReturnFalse_whenUserHasMemberRoleOnly() {

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(savedWorkspace, savedUser)
            );

            boolean exists = workspaceUserRepository
                    .existsByUserIdAndRole(savedUser.getId(), WorkspaceRole.OWNER);

            assertThat(exists).isFalse();
        }

        @Test
        void shouldReturnFalse_whenUserHasNoMembership() {

            boolean exists = workspaceUserRepository
                    .existsByUserIdAndRole(savedUser.getId(), WorkspaceRole.OWNER);

            assertThat(exists).isFalse();
        }
    }

    @Nested
    class FindWorkspacesByUserAndOptionalFiltersTests {

        @Test
        void shouldReturnAllWorkspaces_whenNoFiltersApplied() {

            Workspace secondWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(savedWorkspace, savedUser)
            );
            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(secondWorkspace, savedUser)
            );

            Page<WorkspaceUser> result = workspaceUserRepository
                    .findWorkspacesByUserAndOptionalFilters(
                            savedUser.getId(), null, null, Pageable.unpaged()
                    );

            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        void shouldReturnOnlyOwnerWorkspaces_whenRoleFilterIsOwner() {

            Workspace secondWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(savedWorkspace, savedUser)
            );
            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(secondWorkspace, savedUser)
            );

            Page<WorkspaceUser> result = workspaceUserRepository
                    .findWorkspacesByUserAndOptionalFilters(
                            savedUser.getId(), WorkspaceRole.OWNER, null, Pageable.unpaged()
                    );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getWorkspace().getId())
                    .isEqualTo(savedWorkspace.getId());
        }

        @Test
        void shouldReturnOnlyMemberWorkspaces_whenRoleFilterIsMember() {

            Workspace secondWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(savedWorkspace, savedUser)
            );
            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(secondWorkspace, savedUser)
            );

            Page<WorkspaceUser> result = workspaceUserRepository
                    .findWorkspacesByUserAndOptionalFilters(
                            savedUser.getId(), WorkspaceRole.MEMBER, null, Pageable.unpaged()
                    );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getWorkspace().getId())
                    .isEqualTo(secondWorkspace.getId());
        }

        @Test
        void shouldReturnOnlyArchivedWorkspaces_whenArchivedFilterIsTrue() {

            Workspace archivedWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.archivedWorkspace(null)
            );

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(savedWorkspace, savedUser)
            );
            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(archivedWorkspace, savedUser)
            );

            Page<WorkspaceUser> result = workspaceUserRepository
                    .findWorkspacesByUserAndOptionalFilters(
                            savedUser.getId(), null, true, Pageable.unpaged()
                    );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getWorkspace().getId())
                    .isEqualTo(archivedWorkspace.getId());
        }

        @Test
        void shouldReturnOnlyActiveWorkspaces_whenArchivedFilterIsFalse() {

            Workspace archivedWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.archivedWorkspace(null)
            );

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(savedWorkspace, savedUser)
            );
            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(archivedWorkspace, savedUser)
            );

            Page<WorkspaceUser> result = workspaceUserRepository
                    .findWorkspacesByUserAndOptionalFilters(
                            savedUser.getId(), null, false, Pageable.unpaged()
                    );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getWorkspace().getId())
                    .isEqualTo(savedWorkspace.getId());
        }

        @Test
        void shouldReturnOnlyOwnerArchivedWorkspaces_whenBothFiltersApplied() {

            Workspace archivedWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.archivedWorkspace(null)
            );
            Workspace secondArchivedWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.archivedWorkspace(null)
            );

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(archivedWorkspace, savedUser)
            );
            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(secondArchivedWorkspace, savedUser)
            );

            Page<WorkspaceUser> result = workspaceUserRepository
                    .findWorkspacesByUserAndOptionalFilters(
                            savedUser.getId(), WorkspaceRole.OWNER, true, Pageable.unpaged()
                    );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getWorkspace().getId())
                    .isEqualTo(archivedWorkspace.getId());
        }

        @Test
        void shouldReturnEmpty_whenUserHasNoMemberships() {

            Page<WorkspaceUser> result = workspaceUserRepository
                    .findWorkspacesByUserAndOptionalFilters(
                            savedUser.getId(), null, null, Pageable.unpaged()
                    );

            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        void shouldReturnOnlyWorkspacesForGivenUser_whenMultipleUsersExist() {

            User otherUser = userRepository.save(UserTestDataProvider.user(null));

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(savedWorkspace, savedUser)
            );
            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(savedWorkspace, otherUser)
            );

            Page<WorkspaceUser> result = workspaceUserRepository
                    .findWorkspacesByUserAndOptionalFilters(
                            savedUser.getId(), null, null, Pageable.unpaged()
                    );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getUser().getId()).isEqualTo(savedUser.getId());
        }
    }

    @Nested
    class FindUsersByWorkspaceIdTests {

        @Test
        void shouldReturnAllUsers_whenWorkspaceHasUsers() {

            User secondUser = userRepository.save(UserTestDataProvider.user(null));

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(savedWorkspace, savedUser)
            );
            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.memberWorkspaceUser(savedWorkspace, secondUser)
            );

            List<User> result = workspaceUserRepository
                    .findUsersByWorkspaceId(savedWorkspace.getId());

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(User::getId)
                    .containsExactlyInAnyOrder(savedUser.getId(), secondUser.getId());
        }

        @Test
        void shouldReturnEmptyList_whenWorkspaceHasNoUsers() {

            List<User> result = workspaceUserRepository
                    .findUsersByWorkspaceId(savedWorkspace.getId());

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnOnlyUsersFromGivenWorkspace_whenMultipleWorkspacesExist() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );
            User otherUser = userRepository.save(UserTestDataProvider.user(null));

            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(savedWorkspace, savedUser)
            );
            workspaceUserRepository.save(
                    WorkspaceTestDataProvider.ownerWorkspaceUser(otherWorkspace, otherUser)
            );

            List<User> result = workspaceUserRepository
                    .findUsersByWorkspaceId(savedWorkspace.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getId()).isEqualTo(savedUser.getId());
        }
    }
}
