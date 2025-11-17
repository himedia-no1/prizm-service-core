package run.prizm.core.space.group.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import run.prizm.core.space.group.constraint.GroupChannelPermission;

import java.util.List;

public record GroupUpdateRequest(
        String name,
        List<Long> userIds,
        List<@Valid ChannelPermissionItem> channels
) {
    public record ChannelPermissionItem(
            @NotNull Long channelId,
            @NotNull GroupChannelPermission permission
    ) {
    }
}