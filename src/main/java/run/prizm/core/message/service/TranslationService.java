package run.prizm.core.message.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.message.entity.Message;
import run.prizm.core.message.entity.MessageTranslation;
import run.prizm.core.message.repository.MessageRepository;
import run.prizm.core.message.repository.MessageTranslationRepository;
import run.prizm.core.properties.TranslateProperties;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);
    private final WebClient.Builder webClientBuilder;
    private final MessageRepository messageRepository;
    private final MessageTranslationRepository messageTranslationRepository;
    private final TranslateProperties translateProperties;

    @Transactional(readOnly = true)
    public Mono<String> getOrTranslateMessage(Long messageId, String targetLangCode) {
        Language targetLanguage = resolveLanguage(targetLangCode);
        return findExistingTranslation(messageId, targetLanguage)
                .switchIfEmpty(Mono.defer(() -> translateAndSave(messageId, targetLanguage)));
    }

    private Mono<String> findExistingTranslation(Long messageId, Language targetLanguage) {
        return Mono.fromCallable(() -> messageTranslationRepository.findByMessageIdAndLanguage(messageId, targetLanguage))
                   .subscribeOn(Schedulers.boundedElastic()) // Delegate blocking DB call
                   // 수정된 부분: Optional을 Mono로 올바르게 변환
                   .flatMap(optionalTranslation -> Mono.justOrEmpty(optionalTranslation.map(MessageTranslation::getContent)));
    }

    private Mono<String> translateAndSave(Long messageId, Language targetLanguage) {
        Mono<Message> messageMono = Mono.fromCallable(() -> messageRepository.findById(messageId)
                                                                             .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Message not found with id: " + messageId)))
                                        .subscribeOn(Schedulers.boundedElastic());

        return messageMono.flatMap(message ->
                callExternalTranslationApi(message.getContent(), targetLanguage)
                        .flatMap(translatedContent -> {
                            MessageTranslation newTranslation = MessageTranslation.builder()
                                                                                  .message(message)
                                                                                  .language(targetLanguage)
                                                                                  .content(translatedContent)
                                                                                  .build();

                            return Mono.fromCallable(() -> messageTranslationRepository.save(newTranslation))
                                       .subscribeOn(Schedulers.boundedElastic())
                                       .thenReturn(translatedContent);
                        })
        );
    }

    private Mono<String> callExternalTranslationApi(String text, Language targetLang) {
        WebClient webClient = webClientBuilder.baseUrl(translateProperties.getApiUrl())
                                              .build();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", text);
        requestBody.put("target_lang", targetLang.name()
                                                 .toLowerCase());

        return webClient.post()
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(response -> (String) response.get("result"))
                        .doOnError(error -> logger.error("Translation API call failed", error))
                        .onErrorReturn("Error: Translation failed.");
    }

    private Language resolveLanguage(String targetLangCode) {
        try {
            return Language.from(targetLangCode);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_LANGUAGE_CODE, "Invalid target language code: " + targetLangCode);
        }
    }
}
