package run.prizm.core.message.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import run.prizm.core.message.dto.MessageResponse;

@Service
@RequiredArgsConstructor
public class MessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(MessageConsumer.class);
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * RabbitMQ에서 메시지 이벤트 수신 및 WebSocket 브로드캐스트
     */
    @RabbitListener(queues = "chat.queue")
    public void handleMessageEvent(MessageResponse messageResponse) {
        logger.info("Received message event: messageId={}, channelId={}", 
                messageResponse.getId(), messageResponse.getChannelId());

        // WebSocket 브로드캐스트 destination
        String destination = "/topic/channel/" + messageResponse.getChannelId();
        
        // 모든 채널 구독자에게 메시지 브로드캐스트
        messagingTemplate.convertAndSend(destination, messageResponse);
        logger.info("Broadcasted message to {} - messageId: {}", destination, messageResponse.getId());
    }
}
