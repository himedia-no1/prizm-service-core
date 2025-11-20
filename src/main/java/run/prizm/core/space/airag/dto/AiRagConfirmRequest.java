package run.prizm.core.space.airag.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiRagConfirmRequest {
    
    @NotNull(message = "File key is required")
    private String fileKey;
    
    @NotNull(message = "File name is required")
    private String fileName;
    
    @NotNull(message = "Workspace ID is required")
    private Long workspaceId;
    
    @NotNull(message = "Workspace User ID is required")
    private Long workspaceUserId;
}
