package run.prizm.core.space.workspace.dto;

import run.prizm.core.space.workspace.constraint.WorkspaceUserNotify;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.constraint.WorkspaceUserState;

public record WorkspaceUserSimpleProfileResponse(
        Long id,  // workspaceUserId 추가
        WorkspaceUserRole role,
        WorkspaceUserNotify notifyType,
        WorkspaceUserState state,
        String image,
        String name
) {
}