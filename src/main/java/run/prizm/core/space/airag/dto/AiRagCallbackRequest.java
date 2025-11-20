package run.prizm.core.space.airag.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiRagCallbackRequest {
    private Long fileId;
    private String status;  // SUCCESS or FAILURE
    private Integer chunksCount;
    private Integer vectorsCount;
    private String error;
}
