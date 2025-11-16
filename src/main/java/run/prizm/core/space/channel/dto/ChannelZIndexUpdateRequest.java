package run.prizm.core.space.channel.dto;

public record ChannelZIndexUpdateRequest(
        String position,
        Long beforeId,
        Long afterId
) {}
