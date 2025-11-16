package run.prizm.core.user.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;
import run.prizm.core.user.constraint.UserNotifyType;

import java.time.Instant;

@Entity
@Table(name = "user_notify")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotify {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserNotifyType type;

    @ManyToOne(fetch = FetchType.LAZY)
    private User sender;

    @Column(nullable = false)
    private String content;

    private Long locationId;

    @Column(nullable = false)
    private boolean important;

    @Column(nullable = false)
    private boolean read;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public UserNotify(User receiver, UserNotifyType type, User sender, String content, Long locationId, boolean important, boolean read) {
        this.receiver = receiver;
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.locationId = locationId;
        this.important = important;
        this.read = read;
    }
}