package run.prizm.core.space.airag.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import run.prizm.core.space.airag.constraint.AiRagProgress;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiRagResponse {
    private Long id;
    private Long workspaceId;
    private Long fileId;
    private String fileName;
    private String fileExtension;
    private Long fileSize;
    private String fileUrl;
    private AiRagProgress progress;
    private Instant createdAt;
    private Instant updatedAt;
}
