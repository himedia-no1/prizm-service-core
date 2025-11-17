package run.prizm.core.space.channel.dto;

import run.prizm.core.space.channel.constraint.ChannelType;

import java.time.Instant;

public record ChannelResponse(
        Long id,
        Long workspaceId,
        Long categoryId,
        ChannelType type,
        String name,
        String description,
        String zIndex,
        Instant createdAt
) {
}
