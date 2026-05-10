package com.tempertime.tempertime_api.events.repository;

import com.tempertime.tempertime_api.events.data.EventTestDataProvider;
import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.domain.EventUser;
import com.tempertime.tempertime_api.users.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import com.tempertime.tempertime_api.workspaces.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceRepository;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Transactional
public class EventUserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventUserRepository eventUserRepository;

    private User savedUser;
    private Workspace savedWorkspace;
    private Event savedEvent;

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(UserTestDataProvider.user(null));
        savedWorkspace = workspaceRepository.save(WorkspaceTestDataProvider.workspace(null));
        savedEvent = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
    }

    @Nested
    class ExistsByEventIdAndUserIdTests {

        @Test
        void shouldReturnTrue_whenUserIsAssignedToEvent() {

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));

            boolean result = eventUserRepository
                    .existsByEventIdAndUserId(savedEvent.getId(), savedUser.getId());

            assertThat(result).isTrue();
        }

        @Test
        void shouldReturnFalse_whenUserIsNotAssignedToEvent() {

            boolean result = eventUserRepository
                    .existsByEventIdAndUserId(savedEvent.getId(), savedUser.getId());

            assertThat(result).isFalse();
        }
    }

    // findAllByEventId() (List)

    @Nested
    class FindAllByEventIdListTests {

        @Test
        void shouldReturnAllAssignments_whenEventHasUsers() {

            User secondUser = userRepository.save(UserTestDataProvider.user(null));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, secondUser));

            List<EventUser> result = eventUserRepository.findAllByEventId(savedEvent.getId());

            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(eu -> eu.getUser().getId())
                    .containsExactlyInAnyOrder(savedUser.getId(), secondUser.getId());
        }

        @Test
        void shouldReturnEmptyList_whenEventHasNoUsers() {

            List<EventUser> result = eventUserRepository.findAllByEventId(savedEvent.getId());

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnOnlyAssignmentsForGivenEvent_whenMultipleEventsExist() {

            Event otherEvent = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            User otherUser = userRepository.save(UserTestDataProvider.user(null));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(otherEvent, otherUser));

            List<EventUser> result = eventUserRepository.findAllByEventId(savedEvent.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUser().getId()).isEqualTo(savedUser.getId());
        }
    }

    // findAllByEventId() (Page)

    @Nested
    class FindAllByEventIdPageTests {

        @Test
        void shouldReturnPagedAssignments_whenEventHasUsers() {

            User secondUser = userRepository.save(UserTestDataProvider.user(null));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, secondUser));

            Page<EventUser> result = eventUserRepository
                    .findAllByEventId(savedEvent.getId(), Pageable.unpaged());

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(eu -> eu.getUser().getId())
                    .containsExactlyInAnyOrder(savedUser.getId(), secondUser.getId());
        }

        @Test
        void shouldReturnEmptyPage_whenEventHasNoUsers() {

            Page<EventUser> result = eventUserRepository
                    .findAllByEventId(savedEvent.getId(), Pageable.unpaged());

            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    class CountByEventIdTests {

        @Test
        void shouldReturnCorrectCount_whenEventHasUsers() {

            User secondUser = userRepository.save(UserTestDataProvider.user(null));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, secondUser));

            long count = eventUserRepository.countByEventId(savedEvent.getId());

            assertThat(count).isEqualTo(2);
        }

        @Test
        void shouldReturnZero_whenEventHasNoUsers() {

            long count = eventUserRepository.countByEventId(savedEvent.getId());

            assertThat(count).isEqualTo(0);
        }
    }

    @Nested
    class DeleteByEventIdTests {

        @Test
        void shouldDeleteAllAssignments_whenEventHasUsers() {

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));

            eventUserRepository.deleteByEventId(savedEvent.getId());

            List<EventUser> result = eventUserRepository.findAllByEventId(savedEvent.getId());

            assertThat(result).isEmpty();
        }

        @Test
        void shouldNotAffectOtherEvents_whenDeletingAssignmentsForSpecificEvent() {

            Event otherEvent = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(otherEvent, savedUser));

            eventUserRepository.deleteByEventId(savedEvent.getId());

            List<EventUser> result = eventUserRepository.findAllByEventId(otherEvent.getId());

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    class DeleteByEventIdAndUserIdTests {

        @Test
        void shouldDeleteAssignment_whenUserIsAssignedToEvent() {

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));

            eventUserRepository.deleteByEventIdAndUserId(savedEvent.getId(), savedUser.getId());

            boolean exists = eventUserRepository
                    .existsByEventIdAndUserId(savedEvent.getId(), savedUser.getId());

            assertThat(exists).isFalse();
        }

        @Test
        void shouldNotAffectOtherUsers_whenDeletingSpecificUserAssignment() {

            User otherUser = userRepository.save(UserTestDataProvider.user(null));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, otherUser));

            eventUserRepository.deleteByEventIdAndUserId(savedEvent.getId(), savedUser.getId());

            boolean otherExists = eventUserRepository
                    .existsByEventIdAndUserId(savedEvent.getId(), otherUser.getId());

            assertThat(otherExists).isTrue();
        }
    }

    @Nested
    class DeleteByEventWorkspaceIdAndUserIdTests {

        @Test
        void shouldDeleteAllAssignmentsOfUserInWorkspace() {

            Event secondEvent = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(secondEvent, savedUser));

            eventUserRepository.deleteByEventWorkspaceIdAndUserId(
                    savedWorkspace.getId(), savedUser.getId()
            );

            boolean exists1 = eventUserRepository
                    .existsByEventIdAndUserId(savedEvent.getId(), savedUser.getId());
            boolean exists2 = eventUserRepository
                    .existsByEventIdAndUserId(secondEvent.getId(), savedUser.getId());

            assertThat(exists1).isFalse();
            assertThat(exists2).isFalse();
        }

        @Test
        void shouldNotAffectOtherUsers_whenDeletingAssignmentsForSpecificUser() {

            User otherUser = userRepository.save(UserTestDataProvider.user(null));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, otherUser));

            eventUserRepository.deleteByEventWorkspaceIdAndUserId(
                    savedWorkspace.getId(), savedUser.getId()
            );

            boolean otherExists = eventUserRepository
                    .existsByEventIdAndUserId(savedEvent.getId(), otherUser.getId());

            assertThat(otherExists).isTrue();
        }

        @Test
        void shouldNotAffectOtherWorkspaces_whenDeletingAssignmentsForSpecificWorkspace() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );
            Event otherEvent = eventRepository.save(
                    EventTestDataProvider.event(null, otherWorkspace)
            );

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(otherEvent, savedUser));

            eventUserRepository.deleteByEventWorkspaceIdAndUserId(
                    savedWorkspace.getId(), savedUser.getId()
            );

            boolean otherExists = eventUserRepository
                    .existsByEventIdAndUserId(otherEvent.getId(), savedUser.getId());

            assertThat(otherExists).isTrue();
        }
    }

    @Nested
    class DeleteByUserIdTests {

        @Test
        void shouldDeleteAllAssignmentsOfUser_acrossAllWorkspaces() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );
            Event otherEvent = eventRepository.save(
                    EventTestDataProvider.event(null, otherWorkspace)
            );

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(otherEvent, savedUser));

            eventUserRepository.deleteByUserId(savedUser.getId());

            boolean exists1 = eventUserRepository
                    .existsByEventIdAndUserId(savedEvent.getId(), savedUser.getId());
            boolean exists2 = eventUserRepository
                    .existsByEventIdAndUserId(otherEvent.getId(), savedUser.getId());

            assertThat(exists1).isFalse();
            assertThat(exists2).isFalse();
        }

        @Test
        void shouldNotAffectOtherUsers_whenDeletingAllAssignmentsOfSpecificUser() {

            User otherUser = userRepository.save(UserTestDataProvider.user(null));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, otherUser));

            eventUserRepository.deleteByUserId(savedUser.getId());

            boolean otherExists = eventUserRepository
                    .existsByEventIdAndUserId(savedEvent.getId(), otherUser.getId());

            assertThat(otherExists).isTrue();
        }
    }

    @Nested
    class DeleteByEventWorkspaceIdTests {

        @Test
        void shouldDeleteAllAssignments_whenWorkspaceHasEvents() {

            Event secondEvent = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            User secondUser = userRepository.save(UserTestDataProvider.user(null));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(secondEvent, secondUser));

            eventUserRepository.deleteByEventWorkspaceId(savedWorkspace.getId());

            List<EventUser> result1 = eventUserRepository.findAllByEventId(savedEvent.getId());
            List<EventUser> result2 = eventUserRepository.findAllByEventId(secondEvent.getId());

            assertThat(result1).isEmpty();
            assertThat(result2).isEmpty();
        }

        @Test
        void shouldNotAffectOtherWorkspaces_whenDeletingAssignmentsForSpecificWorkspace() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );
            Event otherEvent = eventRepository.save(
                    EventTestDataProvider.event(null, otherWorkspace)
            );

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(otherEvent, savedUser));

            eventUserRepository.deleteByEventWorkspaceId(savedWorkspace.getId());

            boolean otherExists = eventUserRepository
                    .existsByEventIdAndUserId(otherEvent.getId(), savedUser.getId());

            assertThat(otherExists).isTrue();
        }
    }

    @Nested
    class FindEventIdsByWorkspaceIdAndUserIdTests {

        @Test
        void shouldReturnEventIds_whenUserIsAssignedToEventsInWorkspace() {

            Event secondEvent = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(secondEvent, savedUser));

            Set<Long> result = eventUserRepository.findEventIdsByWorkspaceIdAndUserId(
                    savedWorkspace.getId(), savedUser.getId()
            );

            assertThat(result).containsExactlyInAnyOrder(savedEvent.getId(), secondEvent.getId());
        }

        @Test
        void shouldReturnEmptySet_whenUserHasNoAssignmentsInWorkspace() {

            Set<Long> result = eventUserRepository.findEventIdsByWorkspaceIdAndUserId(
                    savedWorkspace.getId(), savedUser.getId()
            );

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnOnlyEventIdsFromGivenWorkspace_whenUserIsInMultipleWorkspaces() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );
            Event otherEvent = eventRepository.save(
                    EventTestDataProvider.event(null, otherWorkspace)
            );

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(otherEvent, savedUser));

            Set<Long> result = eventUserRepository.findEventIdsByWorkspaceIdAndUserId(
                    savedWorkspace.getId(), savedUser.getId()
            );

            assertThat(result).containsExactly(savedEvent.getId());
        }
    }

    @Nested
    class FindEventIdsByUserIdTests {

        @Test
        void shouldReturnAllEventIds_whenUserIsAssignedToEventsAcrossWorkspaces() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );
            Event otherEvent = eventRepository.save(
                    EventTestDataProvider.event(null, otherWorkspace)
            );

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(otherEvent, savedUser));

            Set<Long> result = eventUserRepository.findEventIdsByUserId(savedUser.getId());

            assertThat(result).containsExactlyInAnyOrder(savedEvent.getId(), otherEvent.getId());
        }

        @Test
        void shouldReturnEmptySet_whenUserHasNoAssignments() {

            Set<Long> result = eventUserRepository.findEventIdsByUserId(savedUser.getId());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class CountUsersByEventIdsTests {

        @Test
        void shouldReturnCorrectCounts_whenEventsHaveUsers() {

            User secondUser = userRepository.save(UserTestDataProvider.user(null));
            Event secondEvent = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, secondUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(secondEvent, savedUser));

            List<Object[]> result = eventUserRepository.countUsersByEventIds(
                    List.of(savedEvent.getId(), secondEvent.getId())
            );

            Map<Long, Long> countMap = result.stream()
                    .collect(Collectors.toMap(
                            row -> (Long) row[0],
                            row -> (Long) row[1]
                    ));

            assertThat(countMap.get(savedEvent.getId())).isEqualTo(2L);
            assertThat(countMap.get(secondEvent.getId())).isEqualTo(1L);
        }

        @Test
        void shouldNotIncludeEventsWithNoUsers_whenEventHasNoAssignments() {

            List<Object[]> result = eventUserRepository.countUsersByEventIds(
                    List.of(savedEvent.getId())
            );

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindUserIdsByEventIdTests {

        @Test
        void shouldReturnUserIds_whenEventHasUsers() {

            User secondUser = userRepository.save(UserTestDataProvider.user(null));

            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(savedEvent, secondUser));

            Set<Long> result = eventUserRepository.findUserIdsByEventId(savedEvent.getId());

            assertThat(result).containsExactlyInAnyOrder(savedUser.getId(), secondUser.getId());
        }

        @Test
        void shouldReturnEmptySet_whenEventHasNoUsers() {

            Set<Long> result = eventUserRepository.findUserIdsByEventId(savedEvent.getId());

            assertThat(result).isEmpty();
        }
    }
}