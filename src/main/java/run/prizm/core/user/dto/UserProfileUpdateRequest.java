package run.prizm.core.user.dto;

import org.springframework.web.multipart.MultipartFile;

public record UserProfileUpdateRequest(
        MultipartFile profileImage,
        String name
) {}
