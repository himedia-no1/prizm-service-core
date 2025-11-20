package run.prizm.core.message.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.message.constraint.MessageType;
import run.prizm.core.message.dto.AIChatRequest;
import run.prizm.core.message.dto.AIChatResponse;
import run.prizm.core.message.dto.MessageResponse;
import run.prizm.core.message.entity.Message;
import run.prizm.core.message.repository.MessageRepository;
import run.prizm.core.space.airag.permission.AiRagPermissionChecker;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.repository.ChannelRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AIAssistantService {

    private static final Logger logger = LoggerFactory.getLogger(AIAssistantService.class);

    private final ChannelRepository channelRepository;
    private final MessageRepository messageRepository;
    private final MessagePublisher messagePublisher;
    private final WebClient.Builder webClientBuilder;
    private final AssistantChannelService assistantChannelService;
    private final AiRagPermissionChecker permissionChecker;
    private final run.prizm.core.properties.UrlProperties urlProperties;

    /**
     * AI 어시스턴트 채팅 처리 (Lazy 채널 생성 포함)
     */
    @Transactional
    public AIChatResponse chat(AIChatRequest request) {
        logger.info("Processing AI chat: workspaceUserId={}, query={}", 
                request.getWorkspaceUserId(), request.getQuery());

        // 1. 권한 체크 (GUEST는 사용 불가)
        permissionChecker.checkAiAssistantPermission(request.getWorkspaceUserId());

        // 2. AI 어시스턴트 채널 조회 또는 생성 (Lazy Creation)
        Channel channel = assistantChannelService.getOrCreateAssistantChannel(request.getWorkspaceUserId());
        
        // 3. 첫 메시지인 경우 채널 제목 생성 (비동기)
        boolean isFirstMessage = messageRepository.countByChannel(channel) == 0;
        if (isFirstMessage) {
            // 제목 생성은 비동기로 처리 (응답 속도 우선)
            assistantChannelService.updateChannelTitle(
                channel.getId(), 
                request.getQuery(), 
                request.getLanguage()
            );
        }

        // 4. FastAPI에 AI 채팅 요청
        Map<String, Object> aiResponse = callAIChatAPI(
                channel.getWorkspace().getId(),
                request.getQuery(),
                request.getLanguage()
        );

        // 4. AI 응답 메시지 저장 (workspaceUser = null)
        String answer = (String) aiResponse.get("answer");
        Message aiMessage = Message.builder()
                .channel(channel)
                .workspaceUser(null)  // AI가 보낸 메시지
                .type(MessageType.TEXT)
                .content(answer)
                .edited(false)
                .pinned(false)
                .build();
        
        aiMessage = messageRepository.save(aiMessage);
        messageRepository.flush(); // Force flush to DB
        logger.info("AI message saved: messageId={}", aiMessage.getId());

        // Convert to DTO
        MessageResponse messageResponse = MessageResponse.from(aiMessage);

        // 5. RabbitMQ로 메시지 브로드캐스트
        messagePublisher.publishMessageCreated(messageResponse);

        // 6. 응답 생성
        List<?> sourcesRaw = (List<?>) aiResponse.get("sources");
        List<AIChatResponse.SourceInfo> sources = sourcesRaw.stream()
                .map(obj -> {
                    Map<String, Object> source = (Map<String, Object>) obj;
                    return new AIChatResponse.SourceInfo(
                            ((Number) source.get("file_id")).longValue(),
                            (String) source.get("text_preview"),
                            ((Number) source.get("score")).doubleValue()
                    );
                })
                .collect(Collectors.toList());

        boolean hasContext = (Boolean) aiResponse.get("has_context");

        return new AIChatResponse(
                aiMessage.getId(),
                answer,
                sources,
                hasContext
        );
    }

    /**
     * FastAPI AI 채팅 API 호출
     */
    private Map<String, Object> callAIChatAPI(Long workspaceId, String query, String language) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(urlProperties.getServiceAiUrl()).build();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("workspace_id", workspaceId);
            requestBody.put("query", query);
            requestBody.put("language", language);
            requestBody.put("search_limit", 5);

            Map response = webClient.post()
                    .uri("/ai/chat")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            logger.info("AI chat API call successful");
            return response;

        } catch (Exception e) {
            logger.error("AI chat API call failed", e);
            throw new BusinessException(ErrorCode.TRANSLATION_FAILED, "AI chat service unavailable");
        }
    }
}
