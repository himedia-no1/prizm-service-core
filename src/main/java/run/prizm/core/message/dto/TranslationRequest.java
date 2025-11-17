package run.prizm.core.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TranslationRequest(
        @NotNull Long messageId,
        @NotBlank String targetLang
) {
}
