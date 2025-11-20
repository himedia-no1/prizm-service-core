package run.prizm.core.file.dto;

public record PresignedUrlResponse(
        String uploadUrl,
        String fileKey,
        String fileName,
        Integer expiresIn
) {
}
