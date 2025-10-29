package run.prizm.auth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "prizm.auth")
public class AuthConfigProperties {

    private App app = new App();
    private Cookie cookie = new Cookie();
    private Jwt jwt = new Jwt();

    @Getter
    @Setter
    public static class App {
        private String frontendUserUrl;
        private String frontendAdminUrl;
    }

    @Getter
    @Setter
    public static class Cookie {
        private boolean httpOnly;
        private String sameSite;
        private long maxAge;
        private boolean secure;
        private String domain;
    }

    @Getter
    @Setter
    public static class Jwt {
        private String secret;
        private long accessTokenExpiration;
        private long refreshTokenExpiration;
    }
}