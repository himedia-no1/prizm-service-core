package run.prizm.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketChannelInterceptor webSocketChannelInterceptor;

    @Value("${spring.rabbitmq.host}")
    private String relayHost;

    @Value("${spring.rabbitmq.stomp-port}")
    private int relayPort;

    @Value("${spring.rabbitmq.username}")
    private String clientLogin;

    @Value("${spring.rabbitmq.password}")
    private String clientPasscode;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Simple in-memory broker 사용 (개발/테스트용)
        // RabbitMQ STOMP 설정 문제로 인해 임시로 Simple Broker 사용
        // 구독 가능한 destination prefixes: /topic, /queue
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트가 publish할 때 사용할 prefix
        // /app/chat.send 형식으로 메시지 전송
        registry.setApplicationDestinationPrefixes("/app");
        
        // 사용자 개별 메시지 전송용 prefix (번역 등)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("http://localhost:3000")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketChannelInterceptor);
    }

}