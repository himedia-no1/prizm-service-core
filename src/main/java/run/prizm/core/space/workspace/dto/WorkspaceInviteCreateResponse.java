package run.prizm.core.space.workspace.dto;

import java.time.Instant;

public record WorkspaceInviteCreateResponse(
        String inviteCode,
        Instant expiresAt,
        Integer maxUses,
        Long allowedUserId
) {
}