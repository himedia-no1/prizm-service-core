package run.prizm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class WebSocketChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketChannelInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        
        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            logger.info("🔍 [SUBSCRIBE] destination: '{}' (sessionId: {})", destination, accessor.getSessionId());
            
            // Frontend에서 이미 /topic/channel/* 형식으로 보내므로 변환 불필요
            // RabbitMQ는 /topic, /queue prefix를 그대로 사용
            if (destination != null && destination.startsWith("/topic/")) {
                logger.info("✅ [SUBSCRIBE] Valid topic destination: '{}'", destination);
            } else if (destination != null && destination.startsWith("/queue/")) {
                logger.info("✅ [SUBSCRIBE] Valid queue destination: '{}'", destination);
            } else if (destination != null && destination.startsWith("/user/")) {
                logger.info("✅ [SUBSCRIBE] Valid user destination: '{}'", destination);
            } else {
                logger.warn("⚠️ [SUBSCRIBE] Invalid destination format: '{}' - must start with /topic/, /queue/, or /user/", destination);
            }
        }
        
        return message;
    }
}
