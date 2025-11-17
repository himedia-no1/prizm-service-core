package run.prizm.core.message.dto;

public record TranslationResponse(
        Long messageId,
        String translatedMessage,
        String originalMessage,
        String targetLang
) {
}
