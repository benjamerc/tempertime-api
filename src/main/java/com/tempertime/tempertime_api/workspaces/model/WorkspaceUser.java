package com.tempertime.tempertime_api.workspaces.model;

import com.tempertime.tempertime_api.users.model.User;
import jakarta.persistence.*;
import lombok.*;

/** Links a user to a workspace and stores their role */
@Entity
@Table(
        name = "workspace_user",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_workspace", "id_user"}),
        indexes = @Index(name = "idx_workspace_user_id_user", columnList = "id_user")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkspaceUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_workspace", nullable = false, foreignKey = @ForeignKey(name = "fk_workspace_user_workspace"))
    private Workspace workspace;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false, foreignKey = @ForeignKey(name = "fk_workspace_user_user"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "workspace_role", nullable = false, length = 20)
    private WorkspaceRole role = WorkspaceRole.MEMBER;
}
