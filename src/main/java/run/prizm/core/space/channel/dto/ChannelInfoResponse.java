package run.prizm.core.space.channel.dto;

import run.prizm.core.space.channel.constraint.ChannelWorkspaceUserNotify;

public record ChannelInfoResponse(
        Long id,
        String name,
        String description,
        ChannelWorkspaceUserNotify myNotify
) {}
