package com.tempertime.tempertime_api.events.repository;

import com.tempertime.tempertime_api.events.model.EventUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventUserRepository extends JpaRepository<EventUser, Long> {

    /** Checks whether a user is assigned to a specific event */
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    /** Retrieves all event-user associations for a given event */
    List<EventUser> findAllByEventId(Long eventId);

    void deleteByEventId(Long eventId);
}
