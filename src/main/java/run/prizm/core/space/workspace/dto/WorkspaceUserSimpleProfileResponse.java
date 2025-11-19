package run.prizm.core.space.workspace.dto;

import run.prizm.core.space.workspace.constraint.WorkspaceUserNotify;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.constraint.WorkspaceUserState;

public record WorkspaceUserSimpleProfileResponse(
        WorkspaceUserRole role,
        WorkspaceUserNotify notifyType,
        WorkspaceUserState state,
        String image,
        String name
) {
}