/**
 * Events module.
 *
 * Contains the domain model, persistence layer, services and controllers
 * for managing events within workspaces, including user assignments,
 * period-based filtering and related validation.
 *
 * Notes:
 * - "UserEvent" refers to read use cases where a user retrieves
 *   all events assigned to them across workspaces, applying
 *   filters such as period. It is not a persistence entity.
 *
 * - "EventUser" is the join entity representing the assignment
 *   between Event and User.
 *
 * - Event date-time values are received with an offset but are
 *   persisted in UTC as an absolute point in time.
 *   When returned, they remain in UTC. The client is responsible
 *   for applying the appropriate offset to display the user's
 *   local time.
 */
package com.tempertime.tempertime_api.events;