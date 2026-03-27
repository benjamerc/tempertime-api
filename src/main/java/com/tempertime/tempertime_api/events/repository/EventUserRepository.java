package com.tempertime.tempertime_api.events.repository;

import com.tempertime.tempertime_api.events.domain.EventUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface EventUserRepository extends JpaRepository<EventUser, Long> {

    /**
     * Checks if a user is assigned to an event.
     */
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    /**
     * Returns all user assignments for a given event.
     * Used internally in service logic.
     */
    List<EventUser> findAllByEventId(Long eventId);

    /**
     * Returns user assignments for a given event with pagination.
     * Intended for API responses only.
     */
    Page<EventUser> findAllByEventId(
            Long eventId,
            Pageable pageable
    );

    /**
     * Returns the number of users assigned to the given event.
     */
    long countByEventId(Long eventId);

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

    /**
     * Deletes all event assignments of a user.
     */
    void deleteByUserId(Long userId);

    /**
     * Deletes all event assignments within a workspace.
     */
    void deleteByEventWorkspaceId(Long workspaceId);

    /**
     * Finds event ids in a workspace
     * where the user is already assigned.
     */
    @Query("""
    SELECT eu.event.id
    FROM EventUser eu
    WHERE eu.event.workspace.id = :workspaceId
      AND eu.user.id = :userId
    """)
    Set<Long> findEventIdsByWorkspaceIdAndUserId(
            @Param("workspaceId") Long workspaceId,
            @Param("userId") Long userId
    );

    /**
     * Finds all event identifiers assigned to a user.
     */
    @Query("""
       SELECT eu.event.id
       FROM EventUser eu
       WHERE eu.user.id = :userId
       """)
    Set<Long> findEventIdsByUserId(Long userId);
}
