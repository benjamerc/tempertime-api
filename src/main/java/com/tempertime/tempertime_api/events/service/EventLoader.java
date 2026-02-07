package com.tempertime.tempertime_api.events.service;

import com.tempertime.tempertime_api.events.exception.EventNotFoundException;
import com.tempertime.tempertime_api.events.model.Event;
import com.tempertime.tempertime_api.events.repository.EventRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Loads Event entities and throws a domain exception if not found */
@Service
@RequiredArgsConstructor
public class EventLoader {

    private final EventRepository eventRepository;

    public Event loadOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new EventNotFoundException("Event not found")
                );
    }
}
