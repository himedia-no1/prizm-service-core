package run.prizm.core.space.category.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryZIndexUpdateRequest(
        @NotBlank String position,
        Long beforeId,
        Long afterId
) {
}