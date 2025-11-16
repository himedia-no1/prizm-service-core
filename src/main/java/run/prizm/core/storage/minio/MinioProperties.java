package run.prizm.core.storage.minio;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "prizm.minio")
public class MinioProperties {
    private String host;
    private Integer port;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String publicUrl;
    
    public String getInternalUrl() {
        return "http://" + host + ":" + port;
    }
    
    public String getPublicUrl() {
        return publicUrl;
    }
}
