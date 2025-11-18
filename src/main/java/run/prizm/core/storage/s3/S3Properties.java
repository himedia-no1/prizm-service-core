package run.prizm.core.storage.s3;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "prizm.s3")
public class S3Properties {
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String region;
    private String publicUrl;
    private String endpoint;
}
