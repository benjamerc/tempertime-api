package com.tempertime.tempertime_api.events.domain;

import com.tempertime.tempertime_api.users.domain.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Persistence entity representing the assignment of a User to an Event.
 *
 * This join entity maps the many-to-many relationship
 * between Event and User.
 *
 * Note: Do not confuse with "UserEvent" terminology, which is
 * a read model used for querying events visible to a user.
 */
@Entity
@Table(
        name = "event_user",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_event_user_event_user",
                        columnNames = {"id_event", "id_user"}
                )
        },
        indexes = {
                @Index(name = "idx_event_user_id_event", columnList = "id_event"),
                @Index(name = "idx_event_user_id_user", columnList = "id_user")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "id_event",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_user_event")
    )
    private Event event;

    @ManyToOne(optional = false)
    @JoinColumn(
            name = "id_user",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_user_user")
    )
    private User user;
}
