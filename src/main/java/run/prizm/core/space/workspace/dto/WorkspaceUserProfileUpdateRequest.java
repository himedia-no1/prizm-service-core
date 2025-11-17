package run.prizm.core.space.workspace.dto;

import org.springframework.web.multipart.MultipartFile;

public record WorkspaceUserProfileUpdateRequest(
        MultipartFile image,
        String name,
        String phone,
        String introduction
) {
}