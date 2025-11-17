package run.prizm.core.storage.minio;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;

import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    public record UploadResult(String path, String originalName, long size, String extension) {}

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
            
            String storedFileName = UUID.randomUUID().toString();
            String path = directory + "/" + storedFileName;
            
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(path)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            
            return new UploadResult(path, nameWithoutExtension, file.getSize(), extension);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e.getMessage());
        }
    }

    public UploadResult uploadFromUrl(String url, String directory) {
        try {
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
            
            String storedFileName = UUID.randomUUID().toString();
            String path = directory + "/" + storedFileName;
            
            InputStream inputStream = new java.net.URL(url).openStream();
            byte[] content = inputStream.readAllBytes();
            inputStream.close();
            
            long fileSize = content.length;
            
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(path)
                            .stream(new java.io.ByteArrayInputStream(content), fileSize, -1)
                            .build()
            );
            
            return new UploadResult(path, nameWithoutExtension, fileSize, extension);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e.getMessage());
        }
    }

    public InputStream getFile(String fileName) {
        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_DOWNLOAD_FAILED, e.getMessage());
        }
    }

    public void deleteFile(String fileName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(fileName)
                            .build()
            );
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_DELETE_FAILED, e.getMessage());
        }
    }

    public String getFileUrl(String fileName) {
        String publicUrl = minioProperties.getPublicUrl();
        
        if (publicUrl != null && !publicUrl.isEmpty()) {
            return publicUrl + "/" + minioProperties.getBucket() + "/" + fileName;
        }
        
        return "/" + minioProperties.getBucket() + "/" + fileName;
    }
}
