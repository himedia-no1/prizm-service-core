package run.prizm.core.file.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PresignedUrlRequest(
        @NotBlank String fileName,
        @NotNull Long fileSize,
        @NotBlank String contentType,
        @NotBlank String directory
) {
}
