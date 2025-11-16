package run.prizm.core.space.workspace.dto;

import java.time.Instant;

public record WorkspaceInviteCreateResponse(
        String code,
        Instant expiresAt,
        Integer maxUses,
        Long channelId
) {
}