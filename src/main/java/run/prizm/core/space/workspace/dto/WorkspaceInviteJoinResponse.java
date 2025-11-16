package run.prizm.core.space.workspace.dto;

public record WorkspaceInviteJoinResponse(
        Long workspaceId,
        Long userId,
        String role
) {
}