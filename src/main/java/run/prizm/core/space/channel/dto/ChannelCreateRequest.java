package run.prizm.core.space.channel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import run.prizm.core.space.channel.constraint.ChannelType;

public record ChannelCreateRequest(
        @NotBlank String name,
        String description,
        @NotNull ChannelType type
) {
}