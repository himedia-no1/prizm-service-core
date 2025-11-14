package run.prizm.auth.common.service.file;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import org.springframework.stereotype.Service;
import run.prizm.auth.config.MinioConfigProperties;

import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioService {

    private final MinioClient minioClient;
    private final MinioConfigProperties minioConfigProperties;

    public MinioService(MinioClient minioClient, MinioConfigProperties minioConfigProperties) {
        this.minioClient = minioClient;
        this.minioConfigProperties = minioConfigProperties;
    }

    public String uploadFile(InputStream inputStream, String contentType, String objectName)
            throws MinioException, InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioConfigProperties.getBucketName()).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioConfigProperties.getBucketName()).build());
            }

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfigProperties.getBucketName())
                            .object(objectName)
                            .stream(inputStream, -1, 10485760) // -1 for unknown size, 10MB part size
                            .contentType(contentType)
                            .build());

            return minioConfigProperties.getEndpoint() + "/" + minioConfigProperties.getBucketName() + "/" + objectName;
        } catch (MinioException e) {
            // Log the exception or rethrow a custom exception
            throw e;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            // Log the exception or rethrow a custom exception
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
