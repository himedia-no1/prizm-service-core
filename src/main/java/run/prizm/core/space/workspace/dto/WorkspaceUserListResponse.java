package run.prizm.core.space.workspace.dto;

import run.prizm.core.space.workspace.constraint.WorkspaceUserState;

import java.util.List;

public record WorkspaceUserListResponse(
        List<WorkspaceUserItem> users
) {
    public record WorkspaceUserItem(
            Long workspaceUserId,
            WorkspaceUserState state,
            String image,
            String name,
            String email
    ) {
    }
}