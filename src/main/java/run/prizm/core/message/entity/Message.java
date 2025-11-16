package run.prizm.core.message.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;
import run.prizm.core.file.entity.File;
import run.prizm.core.message.constraint.MessageType;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.channel.entity.Channel;

import java.time.Instant;

@Entity
@Table(name = "messages")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Channel channel;

    @ManyToOne(fetch = FetchType.LAZY)
    private WorkspaceUser workspaceUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(nullable = false)
    private boolean edited;

    @Column(nullable = false)
    private boolean pinned;

    @ManyToOne(fetch = FetchType.LAZY)
    private File file;

    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    private Message reply;

    @ManyToOne(fetch = FetchType.LAZY)
    private Message thread;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant deletedAt;

    @Builder
    public Message(Channel channel, WorkspaceUser workspaceUser, MessageType type, String content, boolean edited, boolean pinned, File file, Message reply, Message thread) {
        this.channel = channel;
        this.workspaceUser = workspaceUser;
        this.type = type;
        this.content = content;
        this.edited = edited;
        this.pinned = pinned;
        this.file = file;
        this.reply = reply;
        this.thread = thread;
    }
}