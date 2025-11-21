package run.prizm.core.message.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.message.dto.MessageResponse;
import run.prizm.core.message.entity.Message;
import run.prizm.core.message.repository.MessageRepository;
import run.prizm.core.space.channel.repository.ChannelRepository;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final MessagePublisher messagePublisher;

    @Transactional
    public MessageResponse sendMessage(Message message) {
        // Ensure the channel exists before proceeding
        channelRepository.findById(message.getChannel().getId())
                         .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        // Save the message to the database
        Message savedMessage = messageRepository.save(message);
        messageRepository.flush(); // Force flush to DB
        logger.info("💾 Saved message with id: {}", savedMessage.getId());

        // Reload with all relationships eagerly loaded
        Message messageWithRelations = messageRepository.findByIdWithRelations(savedMessage.getId())
                .orElseGet(() -> savedMessage);

        logger.info("🔍 Message after reload - workspaceUser: {}, user: {}, username: {}", 
                messageWithRelations.getWorkspaceUser() != null ? messageWithRelations.getWorkspaceUser().getId() : "null",
                messageWithRelations.getWorkspaceUser() != null && messageWithRelations.getWorkspaceUser().getUser() != null 
                    ? messageWithRelations.getWorkspaceUser().getUser().getId() : "null",
                messageWithRelations.getWorkspaceUser() != null && messageWithRelations.getWorkspaceUser().getUser() != null 
                    ? messageWithRelations.getWorkspaceUser().getUser().getName() : "null");

        // Convert to DTO before transaction ends
        MessageResponse messageResponse = MessageResponse.from(messageWithRelations);
        
        logger.info("📦 MessageResponse created - workspaceUserId: {}, userId: {}, username: {}", 
                messageResponse.getWorkspaceUserId(), messageResponse.getUserId(), messageResponse.getUsername());
        
        // Publish to RabbitMQ - DTO를 전달하므로 Lazy Loading 문제 없음
        messagePublisher.publishMessageCreated(messageResponse);
        
        return messageResponse;
    }
}