package com.tempertime.tempertime_api.events.domain;

/**
 * Defines the user assignment model for an event.
 */
public enum EventScope {

    /**
     * Event assigned automatically to all users in the workspace.
     */
    GLOBAL,

    /**
     * Event assigned only to selected users (via EventUser).
     */
    SPECIFIC
}
