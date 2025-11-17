package run.prizm.core.message.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor
public class TranslationResponse {
    private Long messageId;
    private String translatedMessage;
    private String originalMessage;
    private String targetLang;
}
