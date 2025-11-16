package run.prizm.core.message.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;

import java.time.Instant;

@Entity
@Table(name = "message_translations")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageTranslation {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Message message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Column(nullable = false)
    private String content;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Builder
    public MessageTranslation(Message message, Language language, String content) {
        this.message = message;
        this.language = language;
        this.content = content;
    }
}