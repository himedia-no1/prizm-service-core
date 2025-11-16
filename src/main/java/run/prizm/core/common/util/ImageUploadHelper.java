package run.prizm.core.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.file.entity.File;
import run.prizm.core.file.repository.FileRepository;
import run.prizm.core.storage.minio.MinioService;

@Component
@RequiredArgsConstructor
public class ImageUploadHelper {

    private final MinioService minioService;
    private final FileRepository fileRepository;

    @Transactional
    public File uploadImage(MultipartFile image, String directory) {
        if (image == null || image.isEmpty()) {
            return null;
        }

        try {
            MinioService.UploadResult result = minioService.uploadFile(image, directory);
            
            File file = File.builder()
                    .name(result.originalName())
                    .extension(result.extension())
                    .path(result.path())
                    .size(result.size())
                    .build();
            
            return fileRepository.save(file);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, e.getMessage());
        }
    }

    @Transactional
    public File uploadImageFromUrl(String imageUrl, String directory) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        try {
            MinioService.UploadResult result = minioService.uploadFromUrl(imageUrl, directory);
            
            File file = File.builder()
                    .name(result.originalName())
                    .extension(result.extension())
                    .path(result.path())
                    .size(result.size())
                    .build();
            
            return fileRepository.save(file);
        } catch (Exception e) {
            return null;
        }
    }

    @Transactional
    public void deleteImage(File file) {
        if (file == null) {
            return;
        }
        
        try {
            minioService.deleteFile(file.getPath());
            fileRepository.delete(file);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED, "Failed to delete file: " + e.getMessage());
        }
    }

    public String getImageUrl(File file) {
        if (file == null) {
            return null;
        }
        return minioService.getFileUrl(file.getPath());
    }
}
