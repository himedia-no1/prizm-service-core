package run.prizm.core.message.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;
import run.prizm.core.space.workspace.entity.WorkspaceUser;

import java.time.Instant;

@Entity
@Table(name = "message_emojis")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageEmoji {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Message message;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private WorkspaceUser workspaceUser;

    @Column(nullable = false)
    private String emoji;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public MessageEmoji(Message message, WorkspaceUser workspaceUser, String emoji) {
        this.message = message;
        this.workspaceUser = workspaceUser;
        this.emoji = emoji;
    }
}