package run.prizm.core.space.category.dto;

import java.time.Instant;

public record CategoryResponse(
        Long id,
        Long workspaceId,
        String name,
        String zIndex,
        Instant createdAt
) {
}
