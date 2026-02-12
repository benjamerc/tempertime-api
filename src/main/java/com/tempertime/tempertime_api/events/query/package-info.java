/**
 * Query layer for event read operations that go beyond the Event aggregate.
 *
 * Terminology:
 * - "UserEvent" refers to read use cases where a user retrieves
 *   the events assigned to them.
 * - This is NOT a persistence entity.
 *
 * Do not confuse with EventUser, which is the join entity
 * representing the assignment between Event and User.
 */
package com.tempertime.tempertime_api.events.query;