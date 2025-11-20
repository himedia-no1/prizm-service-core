package run.prizm.core.message.analysis;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.message.constraint.MessageType;
import run.prizm.core.message.entity.Message;
import run.prizm.core.message.repository.MessageRepository;
import run.prizm.core.message.service.MessagePublisher;
import run.prizm.core.properties.UrlProperties;
import run.prizm.core.storage.s3.S3Service;
import run.prizm.core.user.entity.User;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DocumentAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentAnalysisService.class);
    private final MessageRepository messageRepository;
    private final S3Service s3Service;
    private final WebClient.Builder webClientBuilder;
    private final UrlProperties urlProperties;
    private final MessagePublisher messagePublisher;

    /**
     * 문서 분석 비동기 실행
     * 
     * @param messageId 메시지 ID
     */
    @Async
    public CompletableFuture<Void> analyzeDocument(Long messageId) {
        logger.info("Starting document analysis for messageId: {}", messageId);

        try {
            // 1. 메시지 조회
            Message message = messageRepository.findById(messageId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Message not found"));

            // 2. 타입 검증
            if (message.getType() != MessageType.DOCUMENT) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                        "Only DOCUMENT type messages can be analyzed");
            }

            // 3. 파일 존재 확인
            if (message.getFile() == null) {
                throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "No file attached to message");
            }

            // 4. 업로더 언어 조회
            User uploader = message.getWorkspaceUser().getUser();
            Language summaryLanguage = uploader.getLanguage();

            // 5. Presigned GET URL 생성
            String fileKey = message.getFile().getPath();
            String presignedUrl = s3Service.generatePresignedDownloadUrl(fileKey);

            // 6. FastAPI 호출
            String summary = callAnalysisApi(
                    presignedUrl,
                    message.getFile().getName() + "." + message.getFile().getExtension(),
                    summaryLanguage
            );

            // 7. DB 업데이트
            updateMessageContent(messageId, summary);

            // 8. RabbitMQ publish (WebSocket 브로드캐스트)
            Message updatedMessage = messageRepository.findById(messageId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Message not found"));
            messagePublisher.publishMessageAnalyzed(updatedMessage);

            logger.info("Document analysis completed for messageId: {}", messageId);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            logger.error("Document analysis failed for messageId: {}", messageId, e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * FastAPI 문서 분석 API 호출
     */
    private String callAnalysisApi(String fileUrl, String fileName, Language language) {
        WebClient webClient = webClientBuilder.baseUrl(urlProperties.getServiceAiUrl())
                .build();

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("fileUrl", fileUrl);
        requestBody.put("fileName", fileName);
        requestBody.put("summaryLanguage", language.name());

        try {
            Map response = webClient.post()
                    .uri("/ai/analyze")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();  // 비동기 스레드에서 실행중이므로 block 가능

            if (response != null && response.containsKey("summary")) {
                return (String) response.get("summary");
            } else {
                throw new BusinessException(ErrorCode.TRANSLATION_FAILED, "Analysis API returned no summary");
            }
        } catch (Exception e) {
            logger.error("Failed to call analysis API", e);
            throw new BusinessException(ErrorCode.TRANSLATION_FAILED, "Document analysis failed: " + e.getMessage());
        }
    }

    /**
     * 메시지 content 업데이트
     */
    @Transactional
    public void updateMessageContent(Long messageId, String content) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Message not found"));
        
        message.setContent(content);
        messageRepository.save(message);
        
        logger.info("Updated message content for messageId: {}", messageId);
    }
}
