package com.tempertime.tempertime_api.events.repository;

import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.domain.EventScope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** Repository for Event entity */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Finds an event by id within a workspace.
     */
    Optional<Event> findByIdAndWorkspaceId(Long eventId, Long workspaceId);

    /**
     * Finds all events in a workspace by scope.
     */
    List<Event> findByWorkspaceIdAndScope(Long workspaceId, EventScope scope);

    /**
     * Deletes all events within a workspace.
     */
    void deleteByWorkspaceId(Long workspaceId);

    /**
     * Finds all events in a workspace assigned to a specific user.
     */
    @Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN EventUser eu ON eu.event = e
        WHERE e.workspace.id = :workspaceId
          AND eu.user.id = :userId
    """
    )
    Page<Event> findEventsByWorkspaceAndUser(
            @Param("workspaceId") Long workspaceId,
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * Finds all events in a workspace assigned to a user
     * within the given eventDate range.
     */
    @Query("""
    SELECT DISTINCT e
        FROM Event e
        JOIN EventUser eu ON eu.event = e
        WHERE e.workspace.id = :workspaceId
          AND eu.user.id = :userId
          AND e.eventDate >= :start
          AND e.eventDate < :end
    """
    )
    Page<Event> findEventsByWorkspaceAndUserAndDateRange(
            @Param("workspaceId") Long workspaceId,
            @Param("userId") Long userId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable
    );

    /**
     * Finds all events assigned to a user.
     */
    @Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN EventUser eu ON eu.event = e
        WHERE eu.user.id = :userId
    """)
    Page<Event> findAllByUserId(
            @Param("userId") Long userId,
            Pageable pageable
    );

    /**
     * Finds all events assigned to a user
     * within the given eventDate range.
     */
    @Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN EventUser eu ON eu.event = e
        WHERE eu.user.id = :userId
          AND e.eventDate >= :start
          AND e.eventDate < :end
    """)
    Page<Event> findAllByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") Instant start,
            @Param("end") Instant end,
            Pageable pageable
    );
}
