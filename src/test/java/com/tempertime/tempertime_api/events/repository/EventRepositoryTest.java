package com.tempertime.tempertime_api.events.repository;

import com.tempertime.tempertime_api.events.data.EventTestDataProvider;
import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.domain.EventScope;
import com.tempertime.tempertime_api.users.data.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.repository.UserRepository;
import com.tempertime.tempertime_api.workspaces.data.WorkspaceTestDataProvider;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@ActiveProfiles("test")
@DataJpaTest
@Transactional
public class EventRepositoryTest {

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

    @BeforeEach
    void setUp() {
        savedUser = userRepository.save(UserTestDataProvider.user(null));
        savedWorkspace = workspaceRepository.save(WorkspaceTestDataProvider.workspace(null));
    }

    @Nested
    class FindByIdAndWorkspaceIdTests {

        @Test
        void shouldReturnEvent_whenEventExistsInWorkspace() {

            Event savedEvent = eventRepository.save(
                    EventTestDataProvider.event(null, savedWorkspace)
            );

            Optional<Event> result = eventRepository
                    .findByIdAndWorkspaceId(savedEvent.getId(), savedWorkspace.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(savedEvent.getId());
            assertThat(result.get().getWorkspace().getId()).isEqualTo(savedWorkspace.getId());
        }

        @Test
        void shouldReturnEmpty_whenEventDoesNotExistInWorkspace() {

            Optional<Event> result = eventRepository
                    .findByIdAndWorkspaceId(99L, savedWorkspace.getId());

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmpty_whenEventBelongsToAnotherWorkspace() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );
            Event savedEvent = eventRepository.save(
                    EventTestDataProvider.event(null, otherWorkspace)
            );

            Optional<Event> result = eventRepository
                    .findByIdAndWorkspaceId(savedEvent.getId(), savedWorkspace.getId());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByWorkspaceIdAndScopeTests {

        @Test
        void shouldReturnGlobalEvents_whenScopeIsGlobal() {

            eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            eventRepository.save(EventTestDataProvider.specificEvent(null, savedWorkspace));

            List<Event> result = eventRepository
                    .findByWorkspaceIdAndScope(savedWorkspace.getId(), EventScope.GLOBAL);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getScope()).isEqualTo(EventScope.GLOBAL);
        }

        @Test
        void shouldReturnSpecificEvents_whenScopeIsSpecific() {

            eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            eventRepository.save(EventTestDataProvider.specificEvent(null, savedWorkspace));

            List<Event> result = eventRepository
                    .findByWorkspaceIdAndScope(savedWorkspace.getId(), EventScope.SPECIFIC);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getScope()).isEqualTo(EventScope.SPECIFIC);
        }

        @Test
        void shouldReturnEmptyList_whenNoEventsMatchScope() {

            eventRepository.save(EventTestDataProvider.specificEvent(null, savedWorkspace));

            List<Event> result = eventRepository
                    .findByWorkspaceIdAndScope(savedWorkspace.getId(), EventScope.GLOBAL);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnOnlyEventsFromGivenWorkspace_whenMultipleWorkspacesExist() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );

            eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            eventRepository.save(EventTestDataProvider.event(null, otherWorkspace));

            List<Event> result = eventRepository
                    .findByWorkspaceIdAndScope(savedWorkspace.getId(), EventScope.GLOBAL);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getWorkspace().getId()).isEqualTo(savedWorkspace.getId());
        }
    }

    @Nested
    class CountByWorkspaceIdTests {

        @Test
        void shouldReturnCorrectCount_whenWorkspaceHasEvents() {

            eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));

            long count = eventRepository.countByWorkspaceId(savedWorkspace.getId());

            assertThat(count).isEqualTo(2);
        }

        @Test
        void shouldReturnZero_whenWorkspaceHasNoEvents() {

            long count = eventRepository.countByWorkspaceId(savedWorkspace.getId());

            assertThat(count).isEqualTo(0);
        }

        @Test
        void shouldReturnOnlyCountFromGivenWorkspace_whenMultipleWorkspacesExist() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );

            eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            eventRepository.save(EventTestDataProvider.event(null, otherWorkspace));

            long count = eventRepository.countByWorkspaceId(savedWorkspace.getId());

            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    class DeleteByWorkspaceIdTests {

        @Test
        void shouldDeleteAllEvents_whenWorkspaceHasEvents() {

            eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));

            eventRepository.deleteByWorkspaceId(savedWorkspace.getId());

            List<Event> result = eventRepository
                    .findByWorkspaceIdAndScope(savedWorkspace.getId(), EventScope.GLOBAL);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldNotDeleteEvents_whenWorkspaceHasNoEvents() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );
            eventRepository.save(EventTestDataProvider.event(null, otherWorkspace));

            eventRepository.deleteByWorkspaceId(savedWorkspace.getId());

            List<Event> result = eventRepository
                    .findByWorkspaceIdAndScope(otherWorkspace.getId(), EventScope.GLOBAL);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    class FindEventsByWorkspaceAndUserTests {

        @Test
        void shouldReturnPagedEvents_whenUserIsAssignedToEventsInWorkspace() {

            Event event = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            eventUserRepository.save(EventTestDataProvider.eventUser(event, savedUser));

            Page<Event> result = eventRepository.findEventsByWorkspaceAndUser(
                    savedWorkspace.getId(), savedUser.getId(), Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(event.getId());
        }

        @Test
        void shouldReturnEmptyPage_whenUserHasNoEventsInWorkspace() {

            Page<Event> result = eventRepository.findEventsByWorkspaceAndUser(
                    savedWorkspace.getId(), savedUser.getId(), Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        void shouldReturnOnlyEventsFromGivenWorkspace_whenUserIsInMultipleWorkspaces() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );

            Event event = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            Event otherEvent = eventRepository.save(EventTestDataProvider.event(null, otherWorkspace));

            eventUserRepository.save(EventTestDataProvider.eventUser(event, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(otherEvent, savedUser));

            Page<Event> result = eventRepository.findEventsByWorkspaceAndUser(
                    savedWorkspace.getId(), savedUser.getId(), Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getWorkspace().getId())
                    .isEqualTo(savedWorkspace.getId());
        }

        @Test
        void shouldReturnOnlyEventsAssignedToGivenUser_whenMultipleUsersExist() {

            User otherUser = userRepository.save(UserTestDataProvider.user(null));

            Event event = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            Event otherEvent = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));

            eventUserRepository.save(EventTestDataProvider.eventUser(event, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(otherEvent, otherUser));

            Page<Event> result = eventRepository.findEventsByWorkspaceAndUser(
                    savedWorkspace.getId(), savedUser.getId(), Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(event.getId());
        }
    }

    @Nested
    class FindEventsByWorkspaceAndUserAndDateRangeTests {

        @Test
        void shouldReturnEvents_whenEventDateIsWithinRange() {

            Instant now = Instant.now();
            Instant start = now.minusSeconds(3600);
            Instant end = now.plusSeconds(3600);

            Event event = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            eventUserRepository.save(EventTestDataProvider.eventUser(event, savedUser));

            Page<Event> result = eventRepository.findEventsByWorkspaceAndUserAndDateRange(
                    savedWorkspace.getId(), savedUser.getId(), start, end, Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(event.getId());
        }

        @Test
        void shouldReturnEmptyPage_whenEventDateIsOutsideRange() {

            Instant now = Instant.now();
            Instant start = now.plusSeconds(7200);
            Instant end = now.plusSeconds(10800);

            Event event = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            eventUserRepository.save(EventTestDataProvider.eventUser(event, savedUser));

            Page<Event> result = eventRepository.findEventsByWorkspaceAndUserAndDateRange(
                    savedWorkspace.getId(), savedUser.getId(), start, end, Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        void shouldReturnOnlyEventsWithinRange_whenMixedDatesExist() {

            Instant now = Instant.now();
            Instant start = now.minusSeconds(3600);
            Instant end = now.plusSeconds(3600);

            Event eventInRange = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));

            Event eventOutOfRange = Event.builder()
                    .title(EventTestDataProvider.TITLE)
                    .eventDate(now.plusSeconds(7200))
                    .workspace(savedWorkspace)
                    .build();
            eventRepository.save(eventOutOfRange);

            eventUserRepository.save(EventTestDataProvider.eventUser(eventInRange, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(eventOutOfRange, savedUser));

            Page<Event> result = eventRepository.findEventsByWorkspaceAndUserAndDateRange(
                    savedWorkspace.getId(), savedUser.getId(), start, end, Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(eventInRange.getId());
        }
    }

    @Nested
    class FindAllByUserIdTests {

        @Test
        void shouldReturnAllEvents_whenUserIsAssignedToEvents() {

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );

            Event event1 = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            Event event2 = eventRepository.save(EventTestDataProvider.event(null, otherWorkspace));

            eventUserRepository.save(EventTestDataProvider.eventUser(event1, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(event2, savedUser));

            Page<Event> result = eventRepository.findAllByUserId(
                    savedUser.getId(), Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(Event::getId)
                    .containsExactlyInAnyOrder(event1.getId(), event2.getId());
        }

        @Test
        void shouldReturnEmptyPage_whenUserHasNoEvents() {

            Page<Event> result = eventRepository.findAllByUserId(
                    savedUser.getId(), Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        void shouldReturnOnlyEventsAssignedToGivenUser_whenMultipleUsersExist() {

            User otherUser = userRepository.save(UserTestDataProvider.user(null));

            Event event = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            Event otherEvent = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));

            eventUserRepository.save(EventTestDataProvider.eventUser(event, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(otherEvent, otherUser));

            Page<Event> result = eventRepository.findAllByUserId(
                    savedUser.getId(), Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(event.getId());
        }
    }

    @Nested
    class FindAllByUserIdAndDateRangeTests {

        @Test
        void shouldReturnEvents_whenEventDateIsWithinRange() {

            Instant now = Instant.now();
            Instant start = now.minusSeconds(3600);
            Instant end = now.plusSeconds(3600);

            Event event = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            eventUserRepository.save(EventTestDataProvider.eventUser(event, savedUser));

            Page<Event> result = eventRepository.findAllByUserIdAndDateRange(
                    savedUser.getId(), start, end, Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(event.getId());
        }

        @Test
        void shouldReturnEmptyPage_whenEventDateIsOutsideRange() {

            Instant now = Instant.now();
            Instant start = now.plusSeconds(7200);
            Instant end = now.plusSeconds(10800);

            Event event = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            eventUserRepository.save(EventTestDataProvider.eventUser(event, savedUser));

            Page<Event> result = eventRepository.findAllByUserIdAndDateRange(
                    savedUser.getId(), start, end, Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(0);
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        void shouldReturnOnlyEventsWithinRange_whenMixedDatesExist() {

            Instant now = Instant.now();
            Instant start = now.minusSeconds(3600);
            Instant end = now.plusSeconds(3600);

            Event eventInRange = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));

            Event eventOutOfRange = Event.builder()
                    .title(EventTestDataProvider.TITLE)
                    .eventDate(now.plusSeconds(7200))
                    .workspace(savedWorkspace)
                    .build();
            eventRepository.save(eventOutOfRange);

            eventUserRepository.save(EventTestDataProvider.eventUser(eventInRange, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(eventOutOfRange, savedUser));

            Page<Event> result = eventRepository.findAllByUserIdAndDateRange(
                    savedUser.getId(), start, end, Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(eventInRange.getId());
        }

        @Test
        void shouldReturnEventsFromMultipleWorkspaces_whenUserIsInMultipleWorkspaces() {

            Instant now = Instant.now();
            Instant start = now.minusSeconds(3600);
            Instant end = now.plusSeconds(3600);

            Workspace otherWorkspace = workspaceRepository.save(
                    WorkspaceTestDataProvider.workspace(null)
            );

            Event event1 = eventRepository.save(EventTestDataProvider.event(null, savedWorkspace));
            Event event2 = eventRepository.save(EventTestDataProvider.event(null, otherWorkspace));

            eventUserRepository.save(EventTestDataProvider.eventUser(event1, savedUser));
            eventUserRepository.save(EventTestDataProvider.eventUser(event2, savedUser));

            Page<Event> result = eventRepository.findAllByUserIdAndDateRange(
                    savedUser.getId(), start, end, Pageable.unpaged()
            );

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent())
                    .extracting(Event::getId)
                    .containsExactlyInAnyOrder(event1.getId(), event2.getId());
        }
    }
}
