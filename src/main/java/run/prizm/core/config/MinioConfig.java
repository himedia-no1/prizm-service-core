package run.prizm.core.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import run.prizm.core.storage.minio.MinioProperties;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                          .endpoint(minioProperties.getInternalUrl())
                          .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                          .build();
    }
}