package run.prizm.core.space.channel.dto;

import jakarta.validation.constraints.NotNull;
import run.prizm.core.space.channel.constraint.ChannelWorkspaceUserNotify;

public record ChannelNotifyUpdateRequest(
        @NotNull ChannelWorkspaceUserNotify notifyType
) {}
