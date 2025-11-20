package run.prizm.core.space.airag.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AiRagUploadResponse {
    private String uploadUrl;
    private String fileKey;
    private String fileName;
    private Integer expiresIn;
}
