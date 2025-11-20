package run.prizm.core.space.airag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiRagUploadRequest {
    
    @NotNull(message = "Workspace ID is required")
    private Long workspaceId;
    
    @NotNull(message = "Workspace User ID is required")
    private Long workspaceUserId;
    
    @NotNull(message = "File name is required")
    private String fileName;
    
    @NotNull(message = "File size is required")
    private Long fileSize;
    
    private String contentType;
}
