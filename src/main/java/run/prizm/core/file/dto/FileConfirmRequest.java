package run.prizm.core.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record FileConfirmRequest(
        @NotBlank String fileKey,
        @NotBlank String fileName,
        @NotNull Long channelId,
        @NotNull Long workspaceUserId
) {
}
