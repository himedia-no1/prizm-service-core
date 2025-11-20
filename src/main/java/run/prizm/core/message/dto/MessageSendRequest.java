package run.prizm.core.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MessageSendRequest(
        @NotNull Long channelId,
        Long workspaceUserId,  // optional: 없으면 Principal에서 자동으로 찾음
        String contentType,  // optional: 없으면 자동 판별
        @NotBlank String content
) {
}
