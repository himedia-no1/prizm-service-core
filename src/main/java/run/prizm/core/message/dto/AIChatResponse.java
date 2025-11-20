package run.prizm.core.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AIChatResponse {
    private Long messageId;
    private String answer;
    private List<SourceInfo> sources;
    private boolean hasContext;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SourceInfo {
        private Long fileId;
        private String textPreview;
        private Double score;
    }
}
