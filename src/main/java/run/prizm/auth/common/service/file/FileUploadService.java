package run.prizm.auth.common.service.file;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import run.prizm.auth.common.exception.AuthException;
import run.prizm.auth.common.constant.ErrorCode;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {

    private final MinioService minioService;

    // Allowed extensions based on the plan
    private final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", // Images
            "pdf", "txt", // Documents
            "doc", "docx", "xls", "xlsx", "hwp", // Office documents
            "zip" // Archives
    ));

    public FileUploadService(MinioService minioService) {
        this.minioService = minioService;
    }

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new AuthException(ErrorCode.FILE_EMPTY);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new AuthException(ErrorCode.FILE_INVALID_NAME);
        }

        String fileExtension = getFileExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new AuthException(ErrorCode.FILE_UNSUPPORTED_TYPE);
        }

        String objectName = UUID.randomUUID().toString() + "_" + originalFilename;

        try {
            return minioService.uploadFile(file.getInputStream(), file.getContentType(), objectName);
        } catch (IOException e) {
            throw new AuthException(ErrorCode.FILE_UPLOAD_FAILED, "Failed to read file input stream: " + e.getMessage());
        } catch (Exception e) {
            throw new AuthException(ErrorCode.FILE_UPLOAD_FAILED, "MinIO upload failed: " + e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return ""; // No extension
    }
}
