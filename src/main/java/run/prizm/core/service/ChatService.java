package run.prizm.core.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.message.entity.Message;
import run.prizm.core.space.channel.repository.ChannelRepository;
import run.prizm.core.repository.MessageRepository;

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
        channelRepository.findById(message.getChannel().getId()).orElseThrow(
                () -> new RuntimeException("Channel not found with id: " + message.getChannel().getId()));

        // Save the message to the database
        Message savedMessage = messageRepository.save(message);
        logger.info("Saved message with id: {}", savedMessage.getId());

        // Broadcast the original message to all clients in the channel
        String destination = "/topic/channel/" + savedMessage.getChannel().getId();
        messagingTemplate.convertAndSend(destination, savedMessage);
        logger.info("Broadcasted message to {}", destination);
    }
}