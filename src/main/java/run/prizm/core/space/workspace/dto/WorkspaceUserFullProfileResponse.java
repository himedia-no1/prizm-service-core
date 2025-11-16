package run.prizm.core.space.workspace.dto;

import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.constraint.WorkspaceUserState;
import run.prizm.core.user.constraint.UserAuthProvider;

import java.time.Instant;
import java.util.List;

public record WorkspaceUserFullProfileResponse(
        WorkspaceUserRole role,
        WorkspaceUserState state,
        String image,
        String userName,
        String workspaceUserName,
        String email,
        UserAuthProvider authProvider,
        String phone,
        String introduction,
        Instant userCreatedAt,
        Instant workspaceUserCreatedAt,
        List<GroupInfo> groups
) {
    public record GroupInfo(
            Long id,
            String name
    ) {}
}
