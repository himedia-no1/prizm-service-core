package run.prizm.core.message.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.message.entity.Message;
import run.prizm.core.message.repository.MessageRepository;
import run.prizm.core.space.channel.repository.ChannelRepository;

@Service
@RequiredArgsConstructor
public class ChatService {

    private static final Logger logger = LoggerFactory.getLogger(ChatService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;

    @Transactional
    public void sendMessage(Message message) {
        // Ensure the channel exists before proceeding
        channelRepository.findById(message.getChannel()
                                          .getId())
                         .orElseThrow(
                                 () -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        // Save the message to the database
        Message savedMessage = messageRepository.save(message);
        logger.info("Saved message with id: {}", savedMessage.getId());

        // Broadcast the original message to all clients in the channel
        String destination = "/topic/channel/" + savedMessage.getChannel()
                                                             .getId();
        messagingTemplate.convertAndSend(destination, savedMessage);
        logger.info("Broadcasted message to {}", destination);
    }
}