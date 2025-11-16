package run.prizm.core.space.channel.entity;

import jakarta.persistence.*;
import lombok.*;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;
import run.prizm.core.file.entity.File;

@Entity
@Table(name = "channel_webhooks")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChannelWebhook {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Channel channel;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private File image;

    @Column(nullable = false)
    private String secret;

    @Builder
    public ChannelWebhook(Channel channel, String name, File image, String secret) {
        this.channel = channel;
        this.name = name;
        this.image = image;
        this.secret = secret;
    }
}
