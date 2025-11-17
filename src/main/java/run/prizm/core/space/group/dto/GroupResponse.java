package run.prizm.core.space.group.dto;

import java.time.Instant;

public record GroupResponse(
        Long id,
        Long workspaceId,
        String name,
        Instant createdAt
) {
}
