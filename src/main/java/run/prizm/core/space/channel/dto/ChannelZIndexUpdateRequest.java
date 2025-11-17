package run.prizm.core.space.channel.dto;

import jakarta.validation.constraints.NotBlank;

public record ChannelZIndexUpdateRequest(
        @NotBlank String position,
        Long beforeId,
        Long afterId
) {
}