package run.prizm.core.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageSendRequest(
        @NotNull Long channelId,
        @NotNull Long workspaceUserId,
        @NotBlank String contentType,
        @NotBlank String content
) {
}
