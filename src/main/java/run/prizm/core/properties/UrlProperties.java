package run.prizm.core.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "prizm.url")
public class UrlProperties {

    private String prizmWebUser;
    private String prizmServiceCore;
    private String prizmServiceAi;

    public String getWebUserUrl() {
        return prizmWebUser != null && !prizmWebUser.isEmpty() ? prizmWebUser : "";
    }

    public String getServiceCoreUrl() {
        return prizmServiceCore != null && !prizmServiceCore.isEmpty() ? prizmServiceCore : "";
    }

    public String getServiceAiUrl() {
        return prizmServiceAi != null && !prizmServiceAi.isEmpty() ? prizmServiceAi : "";
    }
}
