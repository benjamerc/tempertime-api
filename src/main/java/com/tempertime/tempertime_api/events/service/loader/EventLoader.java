package com.tempertime.tempertime_api.events.service.loader;

import com.tempertime.tempertime_api.events.exception.EventNotFoundException;
import com.tempertime.tempertime_api.events.domain.Event;
import com.tempertime.tempertime_api.events.repository.EventRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Loads Event entities and throws a domain exception if not found.
 */
@Service
@RequiredArgsConstructor
public class EventLoader {

    private final EventRepository eventRepository;

    /**
     * Loads an event by id within the given workspace.
     */
    public Event loadOrThrow(Long workspaceId, Long eventId) {
        return eventRepository
                .findByIdAndWorkspaceId(eventId, workspaceId)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
    }
}
