package run.prizm.core.space.workspace.dto;

import jakarta.validation.constraints.NotNull;
import run.prizm.core.space.workspace.constraint.WorkspaceUserNotify;

public record WorkspaceUserNotifyUpdateRequest(
        @NotNull WorkspaceUserNotify notifyType
) {
}