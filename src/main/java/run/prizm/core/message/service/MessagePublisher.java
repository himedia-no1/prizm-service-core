package run.prizm.core.message.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import run.prizm.core.message.dto.MessageResponse;
import run.prizm.core.message.entity.Message;

@Service
@RequiredArgsConstructor
public class MessagePublisher {

    private static final Logger logger = LoggerFactory.getLogger(MessagePublisher.class);
    private static final String EXCHANGE = "chat.exchange";

    private final RabbitTemplate rabbitTemplate;

    /**
     * 메시지 생성 이벤트 발행
     */
    public void publishMessageCreated(MessageResponse messageResponse) {
        String routingKey = "room." + messageResponse.getChannelId();
        
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, messageResponse);
        logger.info("Published MESSAGE_CREATED event: messageId={}, channelId={}", 
                messageResponse.getId(), messageResponse.getChannelId());
    }

    /**
     * 메시지 분석 완료 이벤트 발행
     */
    public void publishMessageAnalyzed(Message message) {
        String routingKey = "room." + message.getChannel().getId();
        
        MessageResponse messageResponse = MessageResponse.from(message);
        
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, messageResponse);
        logger.info("Published MESSAGE_ANALYZED event: messageId={}, channelId={}", 
                message.getId(), message.getChannel().getId());
    }

    /**
     * 메시지 업데이트 이벤트 발행
     */
    public void publishMessageUpdated(Message message) {
        String routingKey = "room." + message.getChannel().getId();
        
        MessageResponse messageResponse = MessageResponse.from(message);
        
        rabbitTemplate.convertAndSend(EXCHANGE, routingKey, messageResponse);
        logger.info("Published MESSAGE_UPDATED event: messageId={}, channelId={}", 
                message.getId(), message.getChannel().getId());
    }
}
