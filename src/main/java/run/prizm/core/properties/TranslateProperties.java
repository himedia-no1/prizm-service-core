package run.prizm.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "prizm.translate")
public class TranslateProperties {

    /**
     * Translation API endpoint
     */
    private String apiUrl;
}