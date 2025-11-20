package run.prizm.core.config;

import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import run.prizm.core.properties.UrlProperties;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(UrlProperties.class)
public class AppConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                                          .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5 seconds connection timeout
                                          .responseTimeout(Duration.ofSeconds(5)); // 5 seconds response timeout

        return WebClient.builder()
                        .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}