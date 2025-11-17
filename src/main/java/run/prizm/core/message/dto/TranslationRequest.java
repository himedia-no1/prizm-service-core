package run.prizm.core.message.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TranslationRequest {
    private Long messageId;
    private String targetLang;
}
