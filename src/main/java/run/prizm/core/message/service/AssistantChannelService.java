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
import run.prizm.core.space.airag.permission.AiRagPermissionChecker;
import run.prizm.core.space.channel.constraint.ChannelType;
import run.prizm.core.space.channel.constraint.ChannelWorkspaceUserNotify;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.entity.ChannelWorkspaceUser;
import run.prizm.core.space.channel.repository.ChannelRepository;
import run.prizm.core.space.channel.repository.ChannelWorkspaceUserRepository;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.entity.WorkspaceUser;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AssistantChannelService {

    private static final Logger logger = LoggerFactory.getLogger(AssistantChannelService.class);

    private final ChannelRepository channelRepository;
    private final ChannelWorkspaceUserRepository channelWorkspaceUserRepository;
    private final AiRagPermissionChecker permissionChecker;
    private final WebClient.Builder webClientBuilder;
    private final run.prizm.core.properties.UrlProperties urlProperties;

    /**
     * AI 어시스턴트 채널 조회 또는 생성 (Lazy Creation)
     * 
     * @param workspaceUserId 사용자의 WorkspaceUser ID
     * @return AI 어시스턴트 채널
     */
    @Transactional
    public Channel getOrCreateAssistantChannel(Long workspaceUserId) {
        logger.info("Getting or creating AI assistant channel for workspaceUserId={}", workspaceUserId);

        // 1. 권한 체크 (GUEST는 사용 불가)
        permissionChecker.checkAiAssistantPermission(workspaceUserId);

        // 2. WorkspaceUser 조회
        WorkspaceUser workspaceUser = permissionChecker.getWorkspaceUserWithPermission(workspaceUserId);
        Workspace workspace = workspaceUser.getWorkspace();

        // 3. 기존 ASSISTANT 채널 찾기
        Optional<Channel> existingChannel = findExistingAssistantChannel(workspaceUserId);

        if (existingChannel.isPresent()) {
            logger.info("Found existing assistant channel: {}", existingChannel.get().getId());
            return existingChannel.get();
        }

        // 4. 새 ASSISTANT 채널 생성 (제목은 빈 문자열)
        Channel newChannel = createAssistantChannel(workspace, workspaceUser);
        logger.info("Created new assistant channel: {}", newChannel.getId());

        return newChannel;
    }

    /**
     * 기존 AI 어시스턴트 채널 찾기
     */
    private Optional<Channel> findExistingAssistantChannel(Long workspaceUserId) {
        // ChannelWorkspaceUser를 통해 사용자가 참여한 ASSISTANT 채널 찾기
        return channelWorkspaceUserRepository
                .findByWorkspaceUserId(workspaceUserId)
                .stream()
                .map(ChannelWorkspaceUser::getChannel)
                .filter(channel -> channel.getType() == ChannelType.ASSISTANT)
                .filter(channel -> channel.getDeletedAt() == null)
                .findFirst();
    }

    /**
     * 새 AI 어시스턴트 채널 생성 (제목은 빈 문자열로 시작)
     */
    private Channel createAssistantChannel(Workspace workspace, WorkspaceUser workspaceUser) {
        // 1. 채널 생성 (제목은 빈 문자열)
        Channel channel = Channel.builder()
                .workspace(workspace)
                .type(ChannelType.ASSISTANT)
                .name("")  // 빈 제목으로 시작
                .description("Personal AI Assistant")
                .zIndex(BigDecimal.ZERO)
                .build();

        channel = channelRepository.save(channel);

        // 2. ChannelWorkspaceUser 생성 (사용자 참여)
        ChannelWorkspaceUser channelWorkspaceUser = ChannelWorkspaceUser.builder()
                .channel(channel)
                .workspaceUser(workspaceUser)
                .explicit(true)  // DM과 마찬가지로 명시적 참여
                .notify(ChannelWorkspaceUserNotify.ON)  // 기본 알림 설정
                .build();

        channelWorkspaceUserRepository.save(channelWorkspaceUser);

        logger.info("Created ASSISTANT channel: id={}, workspaceUserId={}", 
                channel.getId(), workspaceUser.getId());

        return channel;
    }

    /**
     * 첫 메시지 기반 채널 제목 자동 생성 및 업데이트
     */
    @Transactional
    public void updateChannelTitle(Long channelId, String firstMessage, String language) {
        logger.info("Generating title for channel: channelId={}", channelId);

        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        // 이미 제목이 있으면 업데이트하지 않음
        if (channel.getName() != null && !channel.getName().isEmpty()) {
            logger.info("Channel already has a title, skipping update");
            return;
        }

        // FastAPI로 제목 생성 요청
        String generatedTitle = generateTitleFromAI(firstMessage, language);

        // 채널 제목 업데이트
        channel.setName(generatedTitle);
        channelRepository.save(channel);

        logger.info("Updated channel title: channelId={}, title={}", channelId, generatedTitle);
    }

    /**
     * FastAPI AI 제목 생성 API 호출
     */
    private String generateTitleFromAI(String firstMessage, String language) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(urlProperties.getServiceAiUrl()).build();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("first_message", firstMessage);
            requestBody.put("language", language);

            Map response = webClient.post()
                    .uri("/ai/chat/title")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String title = (String) response.get("title");
            logger.info("Generated title from AI: {}", title);

            return title;

        } catch (Exception e) {
            logger.error("Failed to generate title from AI, using default", e);
            return "AI Chat";  // 실패 시 기본 제목
        }
    }

    /**
     * AI 어시스턴트 채널 존재 여부 확인
     */
    @Transactional(readOnly = true)
    public boolean hasAssistantChannel(Long workspaceUserId) {
        return findExistingAssistantChannel(workspaceUserId).isPresent();
    }

    /**
     * 채널이 AI 어시스턴트 채널인지 확인
     */
    public void validateAssistantChannel(Long channelId) {
        Channel channel = channelRepository.findById(channelId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        if (channel.getType() != ChannelType.ASSISTANT) {
            throw new BusinessException(
                    ErrorCode.INVALID_INPUT_VALUE,
                    "Channel is not an AI assistant channel"
            );
        }
    }
}
