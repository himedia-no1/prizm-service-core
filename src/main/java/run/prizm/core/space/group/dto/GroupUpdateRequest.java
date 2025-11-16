package run.prizm.core.space.group.dto;

import run.prizm.core.space.group.constraint.GroupChannelPermission;

import java.util.List;

public record GroupUpdateRequest(
        String name,
        List<Long> userIds,
        List<ChannelPermissionItem> channels
) {
    public record ChannelPermissionItem(
            Long channelId,
            GroupChannelPermission permission
    ) {}
}
