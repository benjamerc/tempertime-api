package com.tempertime.tempertime_api.events.domain;

/** Defines the scope of an event */
public enum EventScope {

    /** Assigned automatically to all users in the workspace */
    GLOBAL,

    /** Assigned only to selected users (via EventUser) */
    SPECIFIC
}
