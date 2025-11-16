package run.prizm.core.space.group.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.group.constraint.GroupChannelPermission;

import java.time.Instant;

@Entity
@Table(name = "group_channels")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupChannel {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupChannelPermission permission;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public GroupChannel(Group group, Channel channel, GroupChannelPermission permission) {
        this.group = group;
        this.channel = channel;
        this.permission = permission;
    }
}