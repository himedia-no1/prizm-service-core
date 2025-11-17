package run.prizm.core.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLastPathRequest(
        @NotBlank String path
) {
}
