package com.tempertime.tempertime_api.events.repository;

import com.tempertime.tempertime_api.events.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Repository for Event entity */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /** Retrieves all events within a workspace that are assigned to a specific user */
    @Query("""
        select e
        from Event e
        join EventUser eu on eu.event = e
        where e.workspace.id = :workspaceId
          and eu.user.id = :userId
    """)
    List<Event> findEventsByWorkspaceAndUser(
            @Param("workspaceId") Long workspaceId,
            @Param("userId") Long userId
    );
}
