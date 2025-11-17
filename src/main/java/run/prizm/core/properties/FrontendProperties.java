package run.prizm.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "prizm.frontend")
public class FrontendProperties {

    /**
     * 프론트엔드 URL
     */
    private String url;
}
