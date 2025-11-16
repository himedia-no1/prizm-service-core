package run.prizm.core.space.workspace.dto;

import org.springframework.web.multipart.MultipartFile;

public record WorkspaceUpdateRequest(
        MultipartFile image,
        String name
) {}
