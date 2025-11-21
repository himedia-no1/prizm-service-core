package run.prizm.core.message.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.message.constraint.MessageType;
import run.prizm.core.message.dto.TranslationResponse;
import run.prizm.core.message.entity.Message;
import run.prizm.core.message.entity.MessageTranslation;
import run.prizm.core.message.repository.MessageRepository;
import run.prizm.core.message.repository.MessageTranslationRepository;
import run.prizm.core.properties.UrlProperties;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);
    private final WebClient.Builder webClientBuilder;
    private final MessageRepository messageRepository;
    private final MessageTranslationRepository messageTranslationRepository;
    private final UrlProperties urlProperties;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 비동기 번역 및 개인 큐로 전송
     * 
     * @param messageId 메시지 ID
     * @param targetLangCode 대상 언어
     * @param userId 요청 사용자 ID
     */
    @Async
    public CompletableFuture<Void> translateAndNotify(Long messageId, String targetLangCode, String userId) {
        logger.info("Starting async translation: messageId={}, targetLang={}, userId={}", 
                messageId, targetLangCode, userId);

        Language targetLanguage = resolveLanguage(targetLangCode);

        // 1. DB에서 번역 조회
        MessageTranslation existingTranslation = messageTranslationRepository
                .findByMessageIdAndLanguage(messageId, targetLanguage)
                .orElse(null);

        if (existingTranslation != null) {
            // 이미 번역 존재 - 즉시 전송
            sendTranslationToUser(userId, messageId, existingTranslation.getContent(), targetLangCode);
            logger.info("Sent existing translation to user: {}", userId);
            return CompletableFuture.completedFuture(null);
        }

        // 2. 메시지 조회 및 번역 가능 여부 확인
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Message not found"));

        // 번역 가능 타입 검증
        if (!isTranslatable(message)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                    "Message type " + message.getType() + " is not translatable");
        }

        // 3. FastAPI 번역 요청
        try {
            String translatedContent = callExternalTranslationApi(message.getContent(), targetLanguage)
                    .block();  // Reactive → Blocking (비동기 스레드에서 실행중이므로 OK)

            // 4. DB 저장
            MessageTranslation newTranslation = MessageTranslation.builder()
                    .message(message)
                    .language(targetLanguage)
                    .content(translatedContent)
                    .build();
            messageTranslationRepository.save(newTranslation);

            // 5. 개인 큐로 전송
            sendTranslationToUser(userId, messageId, translatedContent, targetLangCode);

            logger.info("Translation completed and sent to user: messageId={}, userId={}", messageId, userId);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            logger.error("Translation failed: messageId={}, userId={}", messageId, userId, e);
            // 에러를 사용자에게 전송
            sendTranslationError(userId, messageId, targetLangCode, e.getMessage());
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * 번역 가능 여부 확인
     */
    private boolean isTranslatable(Message message) {
        MessageType type = message.getType();
        
        // TEXT, LINK는 항상 번역 가능
        if (type == MessageType.TEXT || type == MessageType.LINK) {
            return true;
        }
        
        // DOCUMENT는 content가 있을 때만 (요약본)
        if (type == MessageType.DOCUMENT && message.getContent() != null && !message.getContent().isEmpty()) {
            return true;
        }
        
        // MEDIA, FILE은 번역 불가
        return false;
    }

    /**
     * 개인 큐로 번역 결과 전송
     */
    private void sendTranslationToUser(String userId, Long messageId, String translation, String targetLang) {
        TranslationResponse response = new TranslationResponse(
                messageId,
                translation,
                null,  // original message는 클라이언트가 이미 가지고 있음
                targetLang
        );

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/translation",
                response
        );
        logger.info("Sent translation to /user/{}/queue/translation", userId);
    }

    /**
     * 번역 에러를 개인 큐로 전송
     */
    private void sendTranslationError(String userId, Long messageId, String targetLang, String error) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("messageId", messageId);
        errorResponse.put("targetLang", targetLang);
        errorResponse.put("error", error);
        errorResponse.put("status", "failed");

        messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/translation",
                errorResponse
        );
    }

    // 기존 동기 메서드 (REST API용)
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
        WebClient webClient = webClientBuilder.baseUrl(urlProperties.getServiceAiUrl())
                                              .build();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", text);
        requestBody.put("target_lang", targetLang.name()
                                                 .toLowerCase());

        return webClient.post()
                        .uri("/ai/translate")  // 하드코딩된 경로
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(response -> (String) response.get("result"))
                        .doOnError(error -> logger.error("Translation API call failed", error))
                        .onErrorResume(error -> Mono.error(new BusinessException(ErrorCode.TRANSLATION_FAILED, "External translation service failed.")));
    }

    private Language resolveLanguage(String targetLangCode) {
        try {
            return Language.from(targetLangCode);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_LANGUAGE_CODE, "Invalid target language code: " + targetLangCode);
        }
    }
}
