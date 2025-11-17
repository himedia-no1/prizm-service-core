package run.prizm.core.user.dto;

import jakarta.validation.constraints.NotNull;
import run.prizm.core.common.constraint.Language;

public record UserLanguageUpdateRequest(
        @NotNull Language language
) {
}