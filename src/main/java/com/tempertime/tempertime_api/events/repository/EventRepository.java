package com.tempertime.tempertime_api.events.repository;

import com.tempertime.tempertime_api.events.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Repository for Event entity */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

}
