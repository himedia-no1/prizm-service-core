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
        logger.info("🌐 Starting async translation: messageId={} (type: {}), targetLang={}, userId={}", 
                messageId, messageId != null ? messageId.getClass().getSimpleName() : "null", 
                targetLangCode, userId);

        Language targetLanguage = resolveLanguage(targetLangCode);

        // 1. DB에서 번역 조회
        MessageTranslation existingTranslation = messageTranslationRepository
                .findByMessageIdAndLanguage(messageId, targetLanguage)
                .orElse(null);

        if (existingTranslation != null) {
            // 이미 번역 존재 - 즉시 전송
            sendTranslationToUser(userId, messageId, existingTranslation.getContent(), targetLangCode);
            logger.info("✅ Sent existing translation to user: {}", userId);
            return CompletableFuture.completedFuture(null);
        }

        // 2. 메시지 조회 및 번역 가능 여부 확인
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Message not found"));

        logger.info("📝 Found message: id={}, content={}, type={}", 
                message.getId(), 
                message.getContent() != null ? message.getContent().substring(0, Math.min(50, message.getContent().length())) : "null",
                message.getType());

        // 번역 가능 타입 검증
        if (!isTranslatable(message)) {
            logger.error("❌ Message type {} is not translatable", message.getType());
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                    "Message type " + message.getType() + " is not translatable");
        }

        // 3. FastAPI 번역 요청
        try {
            logger.info("🔄 Calling external translation API: text={}, targetLang={}", 
                    message.getContent().substring(0, Math.min(20, message.getContent().length())), 
                    targetLanguage);
            
            String translatedContent = callExternalTranslationApi(message.getContent(), targetLanguage)
                    .block();  // Reactive → Blocking (비동기 스레드에서 실행중이므로 OK)

            logger.info("✅ Translation API returned: {}", 
                    translatedContent != null ? translatedContent.substring(0, Math.min(50, translatedContent.length())) : "null");

            // 4. DB 저장
            MessageTranslation newTranslation = MessageTranslation.builder()
                    .message(message)
                    .language(targetLanguage)
                    .content(translatedContent)
                    .build();
            messageTranslationRepository.save(newTranslation);

            // 5. 개인 큐로 전송
            sendTranslationToUser(userId, messageId, translatedContent, targetLangCode);

            logger.info("✅ Translation completed and sent to user: messageId={}, userId={}", messageId, userId);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            logger.error("❌ Translation failed: messageId={}, userId={}", messageId, userId, e);
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
        logger.info("🔍 getOrTranslateMessage: messageId={}, targetLang={}", messageId, targetLangCode);
        
        Language targetLanguage = resolveLanguage(targetLangCode);
        return findExistingTranslation(messageId, targetLanguage)
                .switchIfEmpty(Mono.defer(() -> {
                    logger.info("🔄 No existing translation, translating now: messageId={}", messageId);
                    return translateAndSave(messageId, targetLanguage);
                }))
                .doOnNext(result -> logger.info("✅ Translation result ready: messageId={}, length={}", 
                        messageId, result != null ? result.length() : 0))
                .doOnError(error -> logger.error("❌ Translation error: messageId={}, error={}", 
                        messageId, error.getMessage()));
    }

    private Mono<String> findExistingTranslation(Long messageId, Language targetLanguage) {
        return Mono.fromCallable(() -> messageTranslationRepository.findByMessageIdAndLanguage(messageId, targetLanguage))
                   .subscribeOn(Schedulers.boundedElastic()) // Delegate blocking DB call
                   // 수정된 부분: Optional을 Mono로 올바르게 변환
                   .flatMap(optionalTranslation -> Mono.justOrEmpty(optionalTranslation.map(MessageTranslation::getContent)));
    }

    private Mono<String> translateAndSave(Long messageId, Language targetLanguage) {
        logger.info("💾 translateAndSave: messageId={}, targetLang={}", messageId, targetLanguage);
        
        Mono<Message> messageMono = Mono.fromCallable(() -> {
                    logger.info("🔍 Finding message: messageId={}", messageId);
                    return messageRepository.findById(messageId)
                            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                                    "Message not found with id: " + messageId));
                })
                .subscribeOn(Schedulers.boundedElastic());

        return messageMono.flatMap(message -> {
                    logger.info("📝 Message found, calling translation API: messageId={}, content length={}", 
                            messageId, message.getContent() != null ? message.getContent().length() : 0);
                    
                    return callExternalTranslationApi(message.getContent(), targetLanguage)
                            .flatMap(translatedContent -> {
                                logger.info("✅ Translation received, saving to DB: messageId={}", messageId);
                                
                                MessageTranslation newTranslation = MessageTranslation.builder()
                                        .message(message)
                                        .language(targetLanguage)
                                        .content(translatedContent)
                                        .build();

                                return Mono.fromCallable(() -> {
                                            try {
                                                MessageTranslation saved = messageTranslationRepository.save(newTranslation);
                                                logger.info("💾 Translation saved: id={}, messageId={}", 
                                                        saved.getId(), messageId);
                                                return saved.getContent();
                                            } catch (Exception e) {
                                                // 중복 키 에러 발생 시 기존 번역 조회
                                                if (e.getMessage() != null && e.getMessage().contains("duplicate key")) {
                                                    logger.warn("⚠️ Duplicate translation detected, fetching existing: messageId={}, lang={}", 
                                                            messageId, targetLanguage);
                                                    return messageTranslationRepository
                                                            .findByMessageIdAndLanguage(messageId, targetLanguage)
                                                            .map(MessageTranslation::getContent)
                                                            .orElse(translatedContent); // 못 찾으면 방금 번역한 것 반환
                                                }
                                                throw e;
                                            }
                                        })
                                        .subscribeOn(Schedulers.boundedElastic());
                            });
                })
                .doOnError(error -> logger.error("❌ translateAndSave failed: messageId={}, error={}", 
                        messageId, error.getMessage(), error));
    }

    private Mono<String> callExternalTranslationApi(String text, Language targetLang) {
        WebClient webClient = webClientBuilder.baseUrl(urlProperties.getServiceAiUrl())
                                              .build();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("text", text);
        requestBody.put("target_lang", targetLang.name()
                                                 .toLowerCase());

        logger.info("🔗 Calling AI service: url={}/ai/translate, text length={}, targetLang={}", 
                urlProperties.getServiceAiUrl(), text.length(), targetLang.name().toLowerCase());

        return webClient.post()
                        .uri("/ai/translate")  // 하드코딩된 경로
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(response -> {
                            logger.info("✅ AI service response: {}", response);
                            return (String) response.get("result");
                        })
                        .doOnError(error -> logger.error("❌ Translation API call failed", error))
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
