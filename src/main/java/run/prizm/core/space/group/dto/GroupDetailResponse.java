package run.prizm.core.space.group.dto;

import run.prizm.core.space.group.constraint.GroupChannelPermission;

import java.util.List;

public record GroupDetailResponse(
        Long id,
        String name,
        List<UserItem> users,
        List<CategoryWithChannels> categories
) {
    public record UserItem(
            Long id,
            String name
    ) {}
    
    public record CategoryWithChannels(
            Long id,
            String name,
            List<ChannelItem> channels
    ) {}
    
    public record ChannelItem(
            Long id,
            String name,
            GroupChannelPermission permission
    ) {}
}
