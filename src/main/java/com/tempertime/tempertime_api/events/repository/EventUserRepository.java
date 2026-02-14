package com.tempertime.tempertime_api.events.repository;

import com.tempertime.tempertime_api.events.domain.EventUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventUserRepository extends JpaRepository<EventUser, Long> {

    /**
     * Checks if a user is assigned to an event.
     */
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Finds all assignments for an event.
     */
    List<EventUser> findAllByEventId(Long eventId);

    /**
     * Deletes all assignments for an event.
     */
    void deleteByEventId(Long eventId);

    /**
     * Deletes a user assignment from an event.
     */
    void deleteByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Deletes all assignments of a user within a workspace.
     */
    void deleteByEventWorkspaceIdAndUserId(Long workspaceId, Long userId);
}
