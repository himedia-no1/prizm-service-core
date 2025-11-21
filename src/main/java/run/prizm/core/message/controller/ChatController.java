package run.prizm.core.message.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.message.constraint.MessageType;
import run.prizm.core.message.dto.MessageSendRequest;
import run.prizm.core.message.dto.TranslationRequest;
import run.prizm.core.message.dto.TranslationResponse;
import run.prizm.core.message.entity.Message;
import run.prizm.core.message.service.ChatService;
import run.prizm.core.message.service.TranslationService;
import run.prizm.core.message.util.MessageTypeDetector;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.repository.ChannelRepository;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);
    private final ChatService chatService;
    private final TranslationService translationService;
    private final ChannelRepository channelRepository;
    private final WorkspaceUserRepository workspaceUserRepository;

    /**
     * Handles incoming chat messages from clients via WebSocket.
     *
     * @param request The request object containing message details.
     * @param principal Principal 객체 (WebSocket 인증 정보)
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Valid MessageSendRequest request, java.security.Principal principal) {
        logger.info("📩 Received message for channelId={} from user={}", 
                request.channelId(), principal != null ? principal.getName() : "anonymous");
        logger.info("🔍 Request data: channelId={}, workspaceUserId={}, contentType={}, content={}", 
                request.channelId(), request.workspaceUserId(), request.contentType(), 
                request.content() != null ? request.content().substring(0, Math.min(50, request.content().length())) : "null");

        Channel channel = channelRepository.findById(request.channelId())
                                           .orElseThrow(
                                                   () -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        // workspaceUserId가 없으면 에러
        Long workspaceUserId = request.workspaceUserId();
        if (workspaceUserId == null) {
            logger.error("❌ workspaceUserId is null in request");
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, 
                    "workspaceUserId is required");
        }

        logger.info("🔍 Looking up workspaceUser with id: {}", workspaceUserId);
        WorkspaceUser workspaceUser = workspaceUserRepository.findByIdWithUser(workspaceUserId)
                                                             .orElseThrow(
                                                                     () -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));
        
        logger.info("✅ Found workspaceUser: id={}, user.id={}, user.name={}", 
                workspaceUser.getId(), 
                workspaceUser.getUser() != null ? workspaceUser.getUser().getId() : "null",
                workspaceUser.getUser() != null ? workspaceUser.getUser().getName() : "null");

        // 메시지 타입 자동 판별 (contentType이 있으면 사용, 없으면 자동 판별)
        MessageType messageType;
        if (request.contentType() != null && !request.contentType().isEmpty()) {
            try {
                messageType = MessageType.from(request.contentType());
            } catch (IllegalArgumentException ex) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
        } else {
            // 파일이 없는 텍스트 메시지의 경우 자동 판별
            messageType = MessageTypeDetector.determineType(request.content(), null);
        }

        Message message = Message.builder()
                                 .channel(channel)
                                 .workspaceUser(workspaceUser)
                                 .type(messageType)
                                 .content(request.content())
                                 .build();

        chatService.sendMessage(message);
    }

    /**
     * Handles translation requests from clients via WebSocket.
     *
     * @param request The request object containing the message ID and target language.
     * @param principal Principal 객체 (사용자 인증 정보)
     */
    @MessageMapping("/chat.translate")
    public void translate(@Valid TranslationRequest request, java.security.Principal principal) {
        logger.info("Received translation request for messageId: {}, from user: {}", 
                request.messageId(), principal.getName());
        
        // 비동기 번역 시작 (결과는 /user/{userId}/queue/translation로 전송)
        translationService.translateAndNotify(
                request.messageId(),
                request.targetLang(),
                principal.getName()
        );
    }

    /**
     * Handles translation requests via a REST API endpoint (동기 처리).
     *
     * @param request The request object containing the message ID and target language.
     * @return TranslationResponse
     */
    @PostMapping("/api/translate")
    public TranslationResponse handleTranslateApi(@Valid @RequestBody TranslationRequest request) {
        logger.info("📬 Received API translation request for messageId: {}, targetLang: {}", 
                request.messageId(), request.targetLang());
        
        try {
            // Mono를 동기적으로 블로킹 처리
            String translatedText = translationService.getOrTranslateMessage(request.messageId(), request.targetLang())
                    .block(); // Reactive → Blocking 변환
            
            logger.info("✅ Translation completed: messageId={}, result length={}", 
                    request.messageId(), translatedText != null ? translatedText.length() : 0);
            
            return new TranslationResponse(
                    request.messageId(),
                    translatedText,
                    null,
                    request.targetLang()
            );
        } catch (Exception e) {
            logger.error("❌ Translation API failed for messageId={}: {}", 
                    request.messageId(), e.getMessage(), e);
            
            // 에러 발생 시에도 응답 반환 (401 방지)
            return new TranslationResponse(
                    request.messageId(),
                    "Translation failed: " + e.getMessage(),
                    null,
                    request.targetLang()
            );
        }
    }
}
