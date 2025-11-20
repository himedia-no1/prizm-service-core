package run.prizm.core.storage.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final S3Properties s3Properties;

    public UploadResult uploadFile(MultipartFile file, String directory) {
        try {
            String originalFilename = file.getOriginalFilename();
            String nameWithoutExtension = originalFilename;
            String extension = null;

            if (originalFilename != null && originalFilename.contains(".")) {
                int lastDotIndex = originalFilename.lastIndexOf(".");
                nameWithoutExtension = originalFilename.substring(0, lastDotIndex);
                extension = originalFilename.substring(lastDotIndex + 1);
            }

            String storedFileName = UUID.randomUUID()
                                        .toString();
            String path = directory + "/" + storedFileName;

            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest request = PutObjectRequest.builder()
                                                           .bucket(s3Properties.getBucket())
                                                           .key(path)
                                                           .contentType(file.getContentType())
                                                           .acl(ObjectCannedACL.PUBLIC_READ)
                                                           .build();

                s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
            }

            return new UploadResult(path, nameWithoutExtension, file.getSize(), extension);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e.getMessage());
        }
    }

    public UploadResult uploadFromUrl(String url, String directory) {
        try (InputStream inputStream = new URL(url).openStream()) {
            String originalFileName = url.substring(url.lastIndexOf("/") + 1);

            if (originalFileName.contains("?")) {
                originalFileName = originalFileName.substring(0, originalFileName.indexOf("?"));
            }

            String nameWithoutExtension = originalFileName;
            String extension = null;

            if (originalFileName.contains(".")) {
                int lastDotIndex = originalFileName.lastIndexOf(".");
                nameWithoutExtension = originalFileName.substring(0, lastDotIndex);
                extension = originalFileName.substring(lastDotIndex + 1);
            }

            byte[] content = inputStream.readAllBytes();
            long fileSize = content.length;

            String storedFileName = UUID.randomUUID()
                                        .toString();
            String path = directory + "/" + storedFileName;

            PutObjectRequest request = PutObjectRequest.builder()
                                                       .bucket(s3Properties.getBucket())
                                                       .key(path)
                                                       .acl(ObjectCannedACL.PUBLIC_READ)
                                                       .build();

            s3Client.putObject(request, RequestBody.fromBytes(content));

            return new UploadResult(path, nameWithoutExtension, fileSize, extension);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e.getMessage());
        }
    }

    public InputStream getFile(String fileName) {
        try {
            return s3Client.getObject(
                    GetObjectRequest.builder()
                                    .bucket(s3Properties.getBucket())
                                    .key(fileName)
                                    .build()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED, e.getMessage());
        }
    }

    public void deleteFile(String fileName) {
        try {
            DeleteObjectRequest request = DeleteObjectRequest.builder()
                                                             .bucket(s3Properties.getBucket())
                                                             .key(fileName)
                                                             .build();
            s3Client.deleteObject(request);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED, e.getMessage());
        }
    }

    public String getFileUrl(String fileName) {
        String publicUrl = s3Properties.getPublicUrl();

        if (publicUrl != null && !publicUrl.isEmpty()) {
            return publicUrl + "/" + s3Properties.getBucket() + "/" + fileName;
        }

        return "https://" + s3Properties.getBucket()
                + ".s3." + s3Properties.getRegion()
                + ".amazonaws.com/" + fileName;
    }

    /**
     * Presigned PUT URL 생성 (파일 업로드용)
     *
     * @param fileKey S3 key (경로 포함)
     * @param contentType MIME type
     * @return Presigned URL (5분 유효)
     */
    public String generatePresignedUploadUrl(String fileKey, String contentType) {
        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(fileKey)
                    .contentType(contentType)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(5))
                    .putObjectRequest(putRequest)
                    .build();

            PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presignRequest);

            return presigned.url().toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "Failed to generate presigned upload URL: " + e.getMessage());
        }
    }

    /**
     * Presigned GET URL 생성 (파일 다운로드용)
     *
     * @param fileKey S3 key
     * @return Presigned URL (5분 유효)
     */
    public String generatePresignedDownloadUrl(String fileKey) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(fileKey)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(5))
                    .getObjectRequest(getRequest)
                    .build();

            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);

            return presigned.url().toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED, "Failed to generate presigned download URL: " + e.getMessage());
        }
    }

    /**
     * S3 파일 메타데이터 조회
     *
     * @param fileKey S3 key
     * @return HeadObjectResponse
     */
    public HeadObjectResponse getFileMetadata(String fileKey) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(s3Properties.getBucket())
                    .key(fileKey)
                    .build();

            return s3Client.headObject(request);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "File not found in S3: " + fileKey);
        }
    }

    public record UploadResult(String path, String originalName, long size, String extension) {
    }
}