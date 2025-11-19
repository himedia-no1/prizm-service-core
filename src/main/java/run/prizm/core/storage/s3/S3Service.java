package run.prizm.core.storage.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.net.URL;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
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

    public record UploadResult(String path, String originalName, long size, String extension) {
    }
}