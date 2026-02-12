package com.tempertime.tempertime_api.events.repository;

import com.tempertime.tempertime_api.events.model.Event;
import com.tempertime.tempertime_api.events.model.EventScope;
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

    /** Finds an event by id within the given workspace */
    Optional<Event> findByIdAndWorkspaceId(Long eventId, Long workspaceId);

    /** Finds all events in a workspace filtered by scope */
    List<Event> findByWorkspaceIdAndScope(Long workspaceId, EventScope scope);

    /** Retrieves all events within a workspace that are assigned to a specific user */
    @Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN EventUser eu ON eu.event = e
        WHERE e.workspace.id = :workspaceId
          AND eu.user.id = :userId
    """)
    List<Event> findEventsByWorkspaceAndUser(
            @Param("workspaceId") Long workspaceId,
            @Param("userId") Long userId
    );

    /**
     * Retrieves all events within a workspace assigned to a specific user
     * filtered by eventDate range.
     */
    @Query("""
    SELECT DISTINCT e
        FROM Event e
        JOIN EventUser eu ON eu.event = e
        WHERE e.workspace.id = :workspaceId
          AND eu.user.id = :userId
          AND e.eventDate >= :start
          AND e.eventDate < :end
    """)
    List<Event> findEventsByWorkspaceAndUserAndDateRange(
            @Param("workspaceId") Long workspaceId,
            @Param("userId") Long userId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );

    /** Retrieves all events assigned to a user */
    @Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN EventUser eu ON eu.event = e
        WHERE eu.user.id = :userId
    """)
    List<Event> findAllByUserId(
            @Param("userId") Long userId
    );

    /** Retrieves events assigned to a user within the given time range */
    @Query("""
        SELECT DISTINCT e
        FROM Event e
        JOIN EventUser eu ON eu.event = e
        WHERE eu.user.id = :userId
          AND e.eventDate >= :start
          AND e.eventDate < :end
    """)
    List<Event> findAllByUserIdAndDateRange(
            @Param("userId") Long userId,
            @Param("start") Instant start,
            @Param("end") Instant end
    );
}
