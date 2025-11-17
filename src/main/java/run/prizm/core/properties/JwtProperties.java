package run.prizm.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "prizm.auth.jwt")
public class JwtProperties {

    /**
     * JWT 시크릿 키
     */
    private String secret;

    /**
     * Access Token 만료 시간 (밀리초)
     */
    private Long accessTokenExpiration;

    /**
     * Refresh Token 만료 시간 (밀리초)
     */
    private Long refreshTokenExpiration;
}
