package run.prizm.core.space.airag.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import run.prizm.core.common.id.UuidV7LongGeneratedValue;
import run.prizm.core.file.entity.File;
import run.prizm.core.space.airag.constraint.AiRagProgress;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.entity.WorkspaceUser;

import java.time.Instant;

@Entity
@Table(name = "ai_rags")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRag {

    @Id
    @GeneratedValue
    @UuidV7LongGeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Workspace workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private WorkspaceUser workspaceUser;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private File file;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiRagProgress progress;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    private Instant deletedAt;

    @Builder
    public AiRag(Workspace workspace, WorkspaceUser workspaceUser, File file, AiRagProgress progress) {
        this.workspace = workspace;
        this.workspaceUser = workspaceUser;
        this.file = file;
        this.progress = progress;
    }
}