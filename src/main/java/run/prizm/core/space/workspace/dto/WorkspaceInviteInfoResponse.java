package run.prizm.core.space.workspace.dto;

import java.time.Instant;

public record WorkspaceInviteInfoResponse(
        String code,
        Instant createdAt,
        Instant expiresAt,
        Integer usedCount,
        Integer maxCount,
        String location
) {
}