package com.tempertime.tempertime_api.events.model;

import com.tempertime.tempertime_api.users.model.User;
import jakarta.persistence.*;
import lombok.*;

/**
 * Links a user to an event.
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
