package run.prizm.core.space.workspace.dto;

import jakarta.validation.constraints.NotNull;
import run.prizm.core.space.workspace.constraint.WorkspaceUserState;

public record WorkspaceUserStateUpdateRequest(
        @NotNull WorkspaceUserState state
) {
}