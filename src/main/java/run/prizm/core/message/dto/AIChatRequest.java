package run.prizm.core.message.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AIChatRequest {
    private Long workspaceUserId;  // channelId 대신 workspaceUserId 사용 (Lazy 생성)
    private String query;
    private String language = "ko";
}
