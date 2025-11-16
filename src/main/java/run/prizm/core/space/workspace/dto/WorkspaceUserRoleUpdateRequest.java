package run.prizm.core.space.workspace.dto;

import jakarta.validation.constraints.NotNull;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;

public record WorkspaceUserRoleUpdateRequest(
        @NotNull WorkspaceUserRole role
) {}
