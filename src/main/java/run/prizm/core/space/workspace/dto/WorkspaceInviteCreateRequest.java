package run.prizm.core.space.workspace.dto;

import jakarta.validation.constraints.Min;

public record WorkspaceInviteCreateRequest(
        @Min(1) Long expiresInSeconds,
        @Min(1) Integer maxUses,
        Long allowedUserId
) {
}