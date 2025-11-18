package run.prizm.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import run.prizm.core.storage.s3.S3Properties;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final S3Properties s3Properties;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                s3Properties.getAccessKey(),
                s3Properties.getSecretKey()
        );

        S3ClientBuilder builder = S3Client.builder()
                                          .region(Region.of(s3Properties.getRegion()))
                                          .credentialsProvider(StaticCredentialsProvider.create(credentials));

        if (StringUtils.hasText(s3Properties.getEndpoint())) {
            builder = builder.endpointOverride(URI.create(s3Properties.getEndpoint()));
        }

        S3Configuration.Builder serviceConfiguration = S3Configuration.builder();

        if (StringUtils.hasText(s3Properties.getEndpoint())) {
            builder = builder.endpointOverride(URI.create(s3Properties.getEndpoint()));
            serviceConfiguration = serviceConfiguration.pathStyleAccessEnabled(true);
        }

        return builder.serviceConfiguration(serviceConfiguration.build())
                      .build();
    }
}
