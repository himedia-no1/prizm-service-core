package run.prizm.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "prizm.auth.cookie")
public class CookieProperties {

    /**
     * HttpOnly 플래그
     */
    private Boolean httpOnly;

    /**
     * Secure 플래그 (HTTPS에서만 전송)
     */
    private Boolean secure;

    /**
     * SameSite 정책
     */
    private String sameSite;

    /**
     * 쿠키 도메인
     */
    private String domain;

    /**
     * 쿠키 경로
     */
    private String path;
}
