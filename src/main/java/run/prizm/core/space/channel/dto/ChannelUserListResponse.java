package run.prizm.core.space.channel.dto;

import run.prizm.core.space.workspace.constraint.WorkspaceUserState;

import java.util.List;

public record ChannelUserListResponse(
        List<UserItem> regularUsers,
        List<UserItem> guestUsers
) {
    public record UserItem(
            Long id,
            WorkspaceUserState state,
            String image,
            String name
    ) {
    }
}