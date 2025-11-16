package run.prizm.core.space.group.dto;

import jakarta.validation.constraints.NotBlank;

public record GroupCreateRequest(
        @NotBlank String name
) {
}