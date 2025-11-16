package run.prizm.core.space.channel.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UpdateTimestamp;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;
import run.prizm.core.space.category.entity.Category;
import run.prizm.core.space.channel.constraint.ChannelType;
import run.prizm.core.space.workspace.entity.Workspace;

import java.time.Instant;

@Entity
@Table(name = "channels")
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Channel {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType type;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private double zIndex;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant deletedAt;

    @Builder
    public Channel(Workspace workspace, Category category, ChannelType type, String name, String description, double zIndex) {
        this.workspace = workspace;
        this.category = category;
        this.type = type;
        this.name = name;
        this.description = description;
        this.zIndex = zIndex;
    }
}