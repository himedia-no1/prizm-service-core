package run.prizm.core.space.workspace.dto;

import java.time.Instant;

public record WorkspaceResponse(
        Long id,
        String name,
        String imageUrl,
        Instant createdAt
) {
}