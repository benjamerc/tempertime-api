package com.tempertime.tempertime_api.events.service.access;

import com.tempertime.tempertime_api.events.data.EventTestDataProvider;
import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.domain.EventScope;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.events.repository.EventUserRepository;
import com.tempertime.tempertime_api.users.data.UserTestDataProvider;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.service.loader.UserLoader;
import com.tempertime.tempertime_api.workspaces.data.WorkspaceTestDataProvider;
import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import com.tempertime.tempertime_api.workspaces.repository.WorkspaceUserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventAccessServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventUserRepository eventUserRepository;

    @Mock
    private WorkspaceUserRepository workspaceUserRepository;

    @Mock
    private UserLoader userLoader;

    @InjectMocks
    private EventAccessService eventAccessService;

    @Nested
    class AssignUserToGlobalEventsTests {

        @Test
        void shouldAssignUserToGlobalEvents_whenEventsExistAndUserIsNotAssigned() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            Event event = EventTestDataProvider.event(1L, workspace);

            when(eventRepository.findByWorkspaceIdAndScope(workspaceId, EventScope.GLOBAL))
                    .thenReturn(List.of(event));
            when(eventUserRepository.findEventIdsByWorkspaceIdAndUserId(workspaceId, userId))
                    .thenReturn(Set.of());
            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);
            when(eventUserRepository.saveAll(anyList())).thenReturn(List.of());
            when(eventRepository.findAllById(anyList())).thenReturn(List.of(event));
            when(eventUserRepository.countUsersByEventIds(anyList())).thenReturn(List.of());

            eventAccessService.assignUserToGlobalEvents(workspaceId, userId);

            verify(eventUserRepository).saveAll(anyList());
            verify(eventRepository).saveAll(anyList());
        }

        @Test
        void shouldNotAssignUser_whenNoGlobalEventsExist() {

            Long workspaceId = 1L;
            Long userId = 1L;

            when(eventRepository.findByWorkspaceIdAndScope(workspaceId, EventScope.GLOBAL))
                    .thenReturn(List.of());

            eventAccessService.assignUserToGlobalEvents(workspaceId, userId);

            verify(eventUserRepository, never()).saveAll(anyList());
            verify(userLoader, never()).loadUserOrThrow(any());
        }

        @Test
        void shouldNotAssignUser_whenUserIsAlreadyAssignedToAllEvents() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(1L, workspace);

            when(eventRepository.findByWorkspaceIdAndScope(workspaceId, EventScope.GLOBAL))
                    .thenReturn(List.of(event));
            when(eventUserRepository.findEventIdsByWorkspaceIdAndUserId(workspaceId, userId))
                    .thenReturn(Set.of(event.getId()));

            eventAccessService.assignUserToGlobalEvents(workspaceId, userId);

            verify(eventUserRepository, never()).saveAll(anyList());
            verify(userLoader, never()).loadUserOrThrow(any());
        }

        @Test
        void shouldUpdateHasActiveUsers_afterAssigningUser() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(userId);
            Event event = EventTestDataProvider.event(1L, workspace);

            when(eventRepository.findByWorkspaceIdAndScope(workspaceId, EventScope.GLOBAL))
                    .thenReturn(List.of(event));
            when(eventUserRepository.findEventIdsByWorkspaceIdAndUserId(workspaceId, userId))
                    .thenReturn(Set.of());
            when(userLoader.loadUserOrThrow(userId)).thenReturn(user);
            when(eventUserRepository.saveAll(anyList())).thenReturn(List.of());
            when(eventRepository.findAllById(anyList())).thenReturn(List.of(event));

            Object[] row = new Object[]{event.getId(), 2L};
            List<Object[]> countResult = new ArrayList<>();
            countResult.add(row);

            when(eventUserRepository.countUsersByEventIds(anyList()))
                    .thenReturn(countResult);

            eventAccessService.assignUserToGlobalEvents(workspaceId, userId);

            verify(eventRepository).saveAll(argThat(events -> {
                List<Event> eventList = new ArrayList<>((Collection<Event>) events);
                return eventList.size() == 1 && eventList.get(0).getHasActiveUsers();
            }));
        }
    }

    @Nested
    class AssignGlobalEventToAllUsersTests {

        @Test
        void shouldAssignEventToAllUsers_whenUsersExistAndNotAssigned() {

            Long eventId = 1L;
            Long workspaceId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(1L);
            Event event = EventTestDataProvider.event(eventId, workspace);

            when(workspaceUserRepository.findUsersByWorkspaceId(workspaceId))
                    .thenReturn(List.of(user));
            when(eventUserRepository.findUserIdsByEventId(eventId))
                    .thenReturn(Set.of());
            when(eventUserRepository.saveAll(anyList())).thenReturn(List.of());
            when(eventRepository.findAllById(anyList())).thenReturn(List.of(event));
            when(eventUserRepository.countUsersByEventIds(anyList())).thenReturn(List.of());

            eventAccessService.assignGlobalEventToAllUsers(eventId, workspaceId);

            verify(eventUserRepository).saveAll(anyList());
            verify(eventRepository).saveAll(anyList());
        }

        @Test
        void shouldNotAssignEvent_whenNoUsersInWorkspace() {

            Long eventId = 1L;
            Long workspaceId = 1L;

            when(workspaceUserRepository.findUsersByWorkspaceId(workspaceId))
                    .thenReturn(List.of());

            eventAccessService.assignGlobalEventToAllUsers(eventId, workspaceId);

            verify(eventUserRepository, never()).saveAll(anyList());
        }

        @Test
        void shouldNotAssignEvent_whenAllUsersAlreadyAssigned() {

            Long eventId = 1L;
            Long workspaceId = 1L;

            User user = UserTestDataProvider.user(1L);

            when(workspaceUserRepository.findUsersByWorkspaceId(workspaceId))
                    .thenReturn(List.of(user));
            when(eventUserRepository.findUserIdsByEventId(eventId))
                    .thenReturn(Set.of(user.getId()));

            eventAccessService.assignGlobalEventToAllUsers(eventId, workspaceId);

            verify(eventUserRepository, never()).saveAll(anyList());
        }

        @Test
        void shouldUpdateHasActiveUsers_afterAssigningEvent() {

            Long eventId = 1L;
            Long workspaceId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            User user = UserTestDataProvider.user(1L);
            Event event = EventTestDataProvider.event(eventId, workspace);

            when(workspaceUserRepository.findUsersByWorkspaceId(workspaceId))
                    .thenReturn(List.of(user));
            when(eventUserRepository.findUserIdsByEventId(eventId))
                    .thenReturn(Set.of());
            when(eventUserRepository.saveAll(anyList())).thenReturn(List.of());
            when(eventRepository.findAllById(anyList())).thenReturn(List.of(event));

            Object[] row = new Object[]{event.getId(), 2L};
            List<Object[]> countResult = new ArrayList<>();
            countResult.add(row);

            when(eventUserRepository.countUsersByEventIds(anyList()))
                    .thenReturn(countResult);

            eventAccessService.assignGlobalEventToAllUsers(eventId, workspaceId);

            verify(eventRepository).saveAll(argThat(events -> {
                List<Event> eventList = new ArrayList<>((Collection<Event>) events);
                return eventList.size() == 1 && eventList.get(0).getHasActiveUsers();
            }));
        }
    }

    @Nested
    class RemoveUserFromWorkspaceEventsTests {

        @Test
        void shouldRemoveUserFromWorkspaceEvents_whenUserHasAssignments() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(1L, workspace);

            when(eventUserRepository.findEventIdsByWorkspaceIdAndUserId(workspaceId, userId))
                    .thenReturn(Set.of(event.getId()));
            doNothing().when(eventUserRepository)
                    .deleteByEventWorkspaceIdAndUserId(workspaceId, userId);
            when(eventRepository.findAllById(any())).thenReturn(List.of(event));
            when(eventUserRepository.countUsersByEventIds(any())).thenReturn(List.of());

            eventAccessService.removeUserFromWorkspaceEvents(workspaceId, userId);

            verify(eventUserRepository).deleteByEventWorkspaceIdAndUserId(workspaceId, userId);
            verify(eventRepository).saveAll(any());
        }

        @Test
        void shouldNotRemoveUser_whenUserHasNoAssignmentsInWorkspace() {

            Long workspaceId = 1L;
            Long userId = 1L;

            when(eventUserRepository.findEventIdsByWorkspaceIdAndUserId(workspaceId, userId))
                    .thenReturn(Set.of());

            eventAccessService.removeUserFromWorkspaceEvents(workspaceId, userId);

            verify(eventUserRepository, never()).deleteByEventWorkspaceIdAndUserId(any(), any());
            verify(eventRepository, never()).saveAll(anyList());
        }

        @Test
        void shouldUpdateHasActiveUsers_afterRemovingUser() {

            Long workspaceId = 1L;
            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(workspaceId);
            Event event = EventTestDataProvider.event(1L, workspace);

            when(eventUserRepository.findEventIdsByWorkspaceIdAndUserId(workspaceId, userId))
                    .thenReturn(Set.of(event.getId()));
            doNothing().when(eventUserRepository)
                    .deleteByEventWorkspaceIdAndUserId(workspaceId, userId);
            when(eventRepository.findAllById(any())).thenReturn(List.of(event));

            Object[] row = new Object[]{event.getId(), 1L};
            List<Object[]> countResult = new ArrayList<>();
            countResult.add(row);

            when(eventUserRepository.countUsersByEventIds(any()))
                    .thenReturn(countResult);

            eventAccessService.removeUserFromWorkspaceEvents(workspaceId, userId);

            verify(eventRepository).saveAll(argThat(events -> {
                List<Event> eventList = new ArrayList<>((Collection<Event>) events);
                return eventList.size() == 1 && !eventList.get(0).getHasActiveUsers();
            }));
        }
    }

    @Nested
    class RemoveUserFromAllEventsTests {

        @Test
        void shouldRemoveUserFromAllEvents_whenUserHasAssignments() {

            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            Event event = EventTestDataProvider.event(1L, workspace);

            when(eventUserRepository.findEventIdsByUserId(userId))
                    .thenReturn(Set.of(event.getId()));
            doNothing().when(eventUserRepository).deleteByUserId(userId);
            when(eventRepository.findAllById(any())).thenReturn(List.of(event));
            when(eventUserRepository.countUsersByEventIds(any())).thenReturn(List.of());

            eventAccessService.removeUserFromAllEvents(userId);

            verify(eventUserRepository).deleteByUserId(userId);
            verify(eventRepository).saveAll(any());
        }

        @Test
        void shouldNotRemoveUser_whenUserHasNoAssignments() {

            Long userId = 1L;

            when(eventUserRepository.findEventIdsByUserId(userId)).thenReturn(Set.of());

            eventAccessService.removeUserFromAllEvents(userId);

            verify(eventUserRepository, never()).deleteByUserId(any());
            verify(eventRepository, never()).saveAll(anyList());
        }

        @Test
        void shouldUpdateHasActiveUsers_afterRemovingUserFromAllEvents() {

            Long userId = 1L;

            Workspace workspace = WorkspaceTestDataProvider.workspace(1L);
            Event event = EventTestDataProvider.event(1L, workspace);

            when(eventUserRepository.findEventIdsByUserId(userId))
                    .thenReturn(Set.of(event.getId()));
            doNothing().when(eventUserRepository).deleteByUserId(userId);
            when(eventRepository.findAllById(any())).thenReturn(List.of(event));

            Object[] row = new Object[]{event.getId(), 1L};
            List<Object[]> countResult = new ArrayList<>();
            countResult.add(row);

            when(eventUserRepository.countUsersByEventIds(any()))
                    .thenReturn(countResult);

            eventAccessService.removeUserFromAllEvents(userId);

            verify(eventRepository).saveAll(argThat(events -> {
                List<Event> eventList = new ArrayList<>((Collection<Event>) events);
                return eventList.size() == 1 && !eventList.get(0).getHasActiveUsers();
            }));
        }
    }

    @Nested
    class RemoveAllWorkspaceEventsTests {

        @Test
        void shouldDeleteAllEventAssignmentsAndEvents_whenWorkspaceHasEvents() {

            Long workspaceId = 1L;

            doNothing().when(eventUserRepository).deleteByEventWorkspaceId(workspaceId);
            doNothing().when(eventRepository).deleteByWorkspaceId(workspaceId);

            eventAccessService.removeAllWorkspaceEvents(workspaceId);

            verify(eventUserRepository).deleteByEventWorkspaceId(workspaceId);
            verify(eventRepository).deleteByWorkspaceId(workspaceId);
        }

        @Test
        void shouldDeleteAssignmentsBeforeEvents_whenRemovingWorkspaceEvents() {

            Long workspaceId = 1L;

            InOrder inOrder = inOrder(eventUserRepository, eventRepository);

            doNothing().when(eventUserRepository).deleteByEventWorkspaceId(workspaceId);
            doNothing().when(eventRepository).deleteByWorkspaceId(workspaceId);

            eventAccessService.removeAllWorkspaceEvents(workspaceId);

            inOrder.verify(eventUserRepository).deleteByEventWorkspaceId(workspaceId);
            inOrder.verify(eventRepository).deleteByWorkspaceId(workspaceId);
        }
    }
}
