package run.prizm.core.space.channel.dto;

import java.util.List;

public record AccessibleChannelListResponse(
        List<CategoryWithChannels> categories
) {
    public record CategoryWithChannels(
            Long id,
            String name,
            List<ChannelItem> channels
    ) {}
    
    public record ChannelItem(
            Long id,
            String name,
            String permission
    ) {}
}
