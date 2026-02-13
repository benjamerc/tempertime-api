package com.tempertime.tempertime_api.events.domain;

import com.tempertime.tempertime_api.workspaces.domain.Workspace;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/** Event entity */
@Entity
@Table(
        name = "events",
        indexes = @Index(name = "idx_event_workspace", columnList = "id_workspace")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(name = "event_date", nullable = false)
    private Instant eventDate;

    @Column(length = 500)
    private String description;

    /**
     * Scope of the event.
     * By default, assigned to all users in the workspace.
     */
    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EventScope scope = EventScope.GLOBAL;

    /**
     * Indicates whether the event has members assigned other than the owner.
     * The owner is always considered assigned to the event.
     */
    @Builder.Default
    @Column(name = "has_active_users", nullable = false)
    private Boolean hasActiveUsers = true;

    @Column(length = 7)
    private String color;

    @ManyToOne
    @JoinColumn(name = "id_workspace", nullable = false, foreignKey = @ForeignKey(name = "fk_event_workspace"))
    private Workspace workspace;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Transient
    public boolean isExpired() {
        return eventDate.isBefore(Instant.now());
    }
}
