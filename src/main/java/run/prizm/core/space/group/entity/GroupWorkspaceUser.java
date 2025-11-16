package run.prizm.core.space.group.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;
import run.prizm.core.space.workspace.entity.WorkspaceUser;

import java.time.Instant;

@Entity
@Table(name = "group_workspace_users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupWorkspaceUser {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private WorkspaceUser workspaceUser;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public GroupWorkspaceUser(Group group, WorkspaceUser workspaceUser) {
        this.group = group;
        this.workspaceUser = workspaceUser;
    }
}