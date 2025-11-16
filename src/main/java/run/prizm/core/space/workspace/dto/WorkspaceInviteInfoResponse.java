package run.prizm.core.space.workspace.dto;

public record WorkspaceInviteInfoResponse(
        Long workspaceId,
        String workspaceName,
        Long imageId,
        long memberCount
) {
}