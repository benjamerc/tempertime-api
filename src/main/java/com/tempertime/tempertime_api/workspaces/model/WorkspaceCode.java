package com.tempertime.tempertime_api.workspaces.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/** Represents an invitation code tied to a workspace for user joining */
@Entity
@Table(name = "workspace_code")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 8, unique = true)
    private String code;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @OneToOne
    @JoinColumn(name = "id_workspace", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_workspace_code_workspace"))
    private Workspace workspace;
}

