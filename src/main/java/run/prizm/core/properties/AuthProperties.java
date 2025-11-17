package run.prizm.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@Getter
@Setter
@ConfigurationProperties(prefix = "prizm.auth")
public class AuthProperties {

    /**
     * JWT 관련 설정
     */
    @NestedConfigurationProperty
    private JwtProperties jwt = new JwtProperties();

    /**
     * Cookie 관련 설정
     */
    @NestedConfigurationProperty
    private CookieProperties cookie = new CookieProperties();
}
