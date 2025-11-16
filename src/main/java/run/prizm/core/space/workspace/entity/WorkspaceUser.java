package run.prizm.core.space.workspace.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import run.prizm.core.user.entity.User;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;
import run.prizm.core.file.entity.File;
import run.prizm.core.space.workspace.constraint.WorkspaceUserNotify;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.constraint.WorkspaceUserState;

import java.time.Instant;

@Entity
@Table(name = "workspace_users")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkspaceUser {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceUserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    private File image;

    private String name;

    private String email;

    private String phone;

    private String introduction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceUserState state;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkspaceUserNotify notify;

    @Column(nullable = false)
    private boolean banned;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant deletedAt;

    @Builder
    public WorkspaceUser(Workspace workspace, User user, WorkspaceUserRole role, File image,
                         String name, String email, String phone, String introduction, WorkspaceUserState state,
                         WorkspaceUserNotify notify, boolean banned) {
        this.workspace = workspace;
        this.user = user;
        this.role = role;
        this.image = image;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.introduction = introduction;
        this.state = state;
        this.notify = notify;
        this.banned = banned;
    }
}