package com.tempertime.tempertime_api.workspaces.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/** Represents a workspace invite code that allows users to join a workspace */
@Entity
@Table(name = "workspace_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceInviteCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Hashed value of the invite code */
    @Column(name = "invite_code_hash", nullable = false, length = 64, unique = true)
    private String inviteCodeHash;

    /** Whether the invite is currently active */
    @Column(name = "invitations_enabled", nullable = false)
    @Builder.Default
    private Boolean inviteEnabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    /** Workspace associated with this invite code */
    @OneToOne
    @JoinColumn(name = "id_workspace", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_workspace_code_workspace"))
    private Workspace workspace;
}

