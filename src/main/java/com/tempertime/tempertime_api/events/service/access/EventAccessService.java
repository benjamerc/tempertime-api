package com.tempertime.tempertime_api.events.service.access;

import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.domain.EventScope;
import com.tempertime.tempertime_api.events.domain.EventUser;
import com.tempertime.tempertime_api.events.repository.EventRepository;
import com.tempertime.tempertime_api.events.repository.EventUserRepository;
import com.tempertime.tempertime_api.users.domain.User;
import com.tempertime.tempertime_api.users.service.loader.UserLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EventAccessService {

    private final EventRepository  eventRepository;
    private final EventUserRepository eventUserRepository;
    private final UserLoader userLoader;

    /**
     * Assigns the user to all GLOBAL events in the workspace
     * that are not already assigned.
     */
    @Transactional
    public void assignUserToGlobalEvents(Long workspaceId, Long userId) {

        List<Event> globalEvents =
                eventRepository.findByWorkspaceIdAndScope(
                        workspaceId,
                        EventScope.GLOBAL
                );

        if (globalEvents.isEmpty()) {
            return;
        }

        Set<Long> alreadyAssignedEventIds =
                eventUserRepository.findEventIdsByWorkspaceIdAndUserId(
                        workspaceId,
                        userId
                );

        List<Event> eventsToAssign = globalEvents.stream()
                .filter(event -> !alreadyAssignedEventIds.contains(event.getId()))
                .toList();

        if (eventsToAssign.isEmpty()) {
            return;
        }

        User user = userLoader.loadUserOrThrow(userId);

        List<EventUser> newAssignments = eventsToAssign.stream()
                .map(event -> EventUser.builder()
                        .event(event)
                        .user(user)
                        .build())
                .toList();

        eventUserRepository.saveAll(newAssignments);
    }

    /**
     * Removes all event assignments for the user within the specified workspace
     * and updates the hasActiveUsers flag for affected events.
     */
    @Transactional
    public void removeUserFromWorkspaceEvents(Long workspaceId, Long userId) {

        Set<Long> affectedEventIds =
                eventUserRepository.findEventIdsByWorkspaceIdAndUserId(workspaceId, userId);

        if (affectedEventIds.isEmpty()) {
            return;
        }

        eventUserRepository
                .deleteByEventWorkspaceIdAndUserId(workspaceId, userId);

        updateHasActiveUsersByEventIds(affectedEventIds);
    }

    /**
     * Removes all event assignments for the given user
     * and updates the hasActiveUsers flag for affected events.
     */
    @Transactional
    public void removeUserFromAllEvents(Long userId) {

        Set<Long> affectedEventIds =
                eventUserRepository.findEventIdsByUserId(userId);

        if (affectedEventIds.isEmpty()) {
            return;
        }

        eventUserRepository.deleteByUserId(userId);

        updateHasActiveUsersByEventIds(affectedEventIds);
    }

    /**
     * Removes all events and their assignments
     * within the specified workspace.
     */
    @Transactional
    public void removeAllWorkspaceEvents(Long workspaceId) {

        eventUserRepository.deleteByEventWorkspaceId(workspaceId);
        eventRepository.deleteByWorkspaceId(workspaceId);
    }

    /**
     * Updates the hasActiveUsers flag for the given events
     * based on their current user assignments.
     */
    private void updateHasActiveUsersByEventIds(Set<Long> eventIds) {

        List<Event> events = eventRepository.findAllById(eventIds);

        for (Event event : events) {
            long assignedCount =
                    eventUserRepository.countByEventId(event.getId());

            event.setHasActiveUsers(assignedCount > 1);
        }

        eventRepository.saveAll(events);
    }
}
