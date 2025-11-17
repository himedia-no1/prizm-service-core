package run.prizm.core.controller;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import run.prizm.core.message.constraint.MessageType;
import run.prizm.core.message.dto.MessageSendRequest;
import run.prizm.core.message.dto.TranslationResponse;
import run.prizm.core.message.dto.TranslationRequest;
import run.prizm.core.message.entity.Message;
import run.prizm.core.service.ChatService;
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
    private final MessageTypeRepository messageTypeRepository; // To fetch MessageType entity
    private final ChannelRepository channelRepository;
    private final WorkspaceUserRepository workspaceUserRepository;

    /**
     * Handles incoming chat messages from clients via WebSocket.
     * @param request The request object containing message details.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(MessageSendRequest request) {
        logger.info("📩 Received message for channelId={}", request.getChannelId());

        Channel channel = channelRepository.findById(request.getChannelId()).orElseThrow(
                () -> new RuntimeException("Channel not found with id: " + request.getChannelId()));

        WorkspaceUser workspaceUser = workspaceUserRepository.findById(request.getWorkspaceUserId()).orElseThrow(
                () -> new RuntimeException("WorkspaceUser not found with id: " + request.getWorkspaceUserId()));

        MessageType messageType = messageTypeRepository.findById(request.getContentType())
                .orElseThrow(() -> new RuntimeException("Invalid message type: " + request.getContentType()));

        Message message = Message.builder()
                .channel(channel)
                .workspaceUser(workspaceUser)
                .type(messageType)
                .content(request.getContent())
                .build();

        chatService.sendMessage(message);
    }

    /**
     * Handles translation requests from clients via WebSocket.
     * @param request The request object containing the message ID and target language.
     * @return A Mono containing the translation response.
     */
    @MessageMapping("/chat.translate")
    @SendToUser("/queue/translate")
    public Mono<TranslationResponse> translate(TranslationRequest request) {
        logger.info("Received translation request for messageId: {}", request.getMessageId());
        // Original message content is not available here directly, would need another fetch if required for the response.
        // For simplicity, returning null for original message.
        return translationService.getOrTranslateMessage(request.getMessageId(), request.getTargetLang())
                .map(translatedText -> new TranslationResponse(
                        request.getMessageId(),
                        translatedText,
                        null, // Original message would require another DB lookup
                        request.getTargetLang()
                ));
    }

    /**
     * Handles translation requests via a REST API endpoint.
     * @param request The request object containing the message ID and target language.
     * @return A Mono containing the translation response.
     */
    @PostMapping("/api/translate")
    public Mono<TranslationResponse> handleTranslateApi(@RequestBody TranslationRequest request) {
        logger.info("Received API translation request for messageId: {}", request.getMessageId());
        return translationService.getOrTranslateMessage(request.getMessageId(), request.getTargetLang())
                .map(translatedText -> new TranslationResponse(
                        request.getMessageId(),
                        translatedText,
                        null, // Original message would require another DB lookup
                        request.getTargetLang()
                ));
    }
}