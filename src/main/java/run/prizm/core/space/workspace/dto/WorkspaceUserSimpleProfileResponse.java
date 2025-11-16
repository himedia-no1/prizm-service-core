package run.prizm.core.space.workspace.dto;

import run.prizm.core.space.workspace.constraint.WorkspaceUserNotify;
import run.prizm.core.space.workspace.constraint.WorkspaceUserState;

public record WorkspaceUserSimpleProfileResponse(
        WorkspaceUserNotify notifyType,
        WorkspaceUserState state,
        String image,
        String name
) {}
