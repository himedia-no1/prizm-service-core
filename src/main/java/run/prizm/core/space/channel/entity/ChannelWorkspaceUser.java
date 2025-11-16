package run.prizm.core.space.channel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;
import run.prizm.core.space.channel.constraint.ChannelWorkspaceUserNotify;
import run.prizm.core.space.workspace.entity.WorkspaceUser;

import java.time.Instant;

@Entity
@Table(name = "channel_workspace_users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelWorkspaceUser {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private WorkspaceUser workspaceUser;

    @Column(nullable = false)
    private boolean explicit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelWorkspaceUserNotify notify;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public ChannelWorkspaceUser(Channel channel, WorkspaceUser workspaceUser, boolean explicit, ChannelWorkspaceUserNotify notify) {
        this.channel = channel;
        this.workspaceUser = workspaceUser;
        this.explicit = explicit;
        this.notify = notify;
    }
}