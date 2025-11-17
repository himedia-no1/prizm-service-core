package run.prizm.core.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.message.entity.Message;
import run.prizm.core.message.entity.MessageTranslation;
import run.prizm.core.repository.LanguageRepository;
import run.prizm.core.repository.MessageRepository;
import run.prizm.core.repository.MessageTranslationRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);
    private final WebClient.Builder webClientBuilder;
    private final MessageRepository messageRepository;
    private final MessageTranslationRepository messageTranslationRepository;
    private final LanguageRepository languageRepository;

    @Value("${translation.api.url}")
    private String apiUrl;

    @Transactional(readOnly = true)
    public Mono<String> getOrTranslateMessage(Long messageId, String targetLangCode) {
        // 1. Find existing translation in a non-blocking way
        return findExistingTranslation(messageId, targetLangCode)
                // 2. If not found, switch to the translation logic
                .switchIfEmpty(Mono.defer(() -> translateAndSave(messageId, targetLangCode)));
    }

    private Mono<String> findExistingTranslation(Long messageId, String targetLangCode) {
        return Mono.fromCallable(() -> messageTranslationRepository.findByMessageIdAndLanguageCode(messageId, targetLangCode))
                .subscribeOn(Schedulers.boundedElastic()) // Delegate blocking DB call
                // 수정된 부분: Optional을 Mono로 올바르게 변환
                .flatMap(optionalTranslation -> Mono.justOrEmpty(optionalTranslation.map(MessageTranslation::getContent)));
    }

    private Mono<String> translateAndSave(Long messageId, String targetLangCode) {
        // Fetch the original message
        Mono<Message> messageMono = Mono.fromCallable(() -> messageRepository.findById(messageId)
                        .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId)))
                .subscribeOn(Schedulers.boundedElastic());

        // Fetch the language entity
        Mono<Language> languageMono = Mono.fromCallable(() -> languageRepository.findById(targetLangCode)
                        .orElseGet(() -> {
                            Language newLang = new Language();
                            newLang.setCode(targetLangCode);
                            return languageRepository.save(newLang);
                        }))
                .subscribeOn(Schedulers.boundedElastic());

        return messageMono.flatMap(message ->
                // Call external API
                callExternalTranslationApi(message.getContent(), targetLangCode)
                        .zipWith(languageMono) // Combine API result with language entity
                        .flatMap(tuple -> {
                            String translatedContent = tuple.getT1();
                            Language targetLanguage = tuple.getT2();

                            MessageTranslation newTranslation = MessageTranslation.builder()
                                    .message(message)
                                    .language(targetLanguage)
                                    .content(translatedContent)
                                    .build();

                            // Save the new translation (blocking call)
                            return Mono.fromRunnable(() -> messageTranslationRepository.save(newTranslation))
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .thenReturn(translatedContent); // After saving, return the content
                        })
        );
    }

    private Mono<String> callExternalTranslationApi(String text, String targetLang) {
        WebClient webClient = webClientBuilder.baseUrl(apiUrl).build();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", text);
        if (targetLang != null && !targetLang.isEmpty()) {
            requestBody.put("target_lang", targetLang);
        }

        return webClient.post()
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("result"))
                .doOnError(error -> logger.error("Translation API call failed", error))
                .onErrorReturn("Error: Translation failed.");
    }
}