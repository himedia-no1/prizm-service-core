package run.prizm.core.file.controller;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.file.dto.FileConfirmRequest;
import run.prizm.core.file.dto.PresignedUrlRequest;
import run.prizm.core.file.dto.PresignedUrlResponse;
import run.prizm.core.file.entity.File;
import run.prizm.core.file.repository.FileRepository;
import run.prizm.core.message.constraint.MessageType;
import run.prizm.core.message.entity.Message;
import run.prizm.core.message.repository.MessageRepository;
import run.prizm.core.message.service.ChatService;
import run.prizm.core.message.util.MessageTypeDetector;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.repository.ChannelRepository;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;
import run.prizm.core.storage.s3.S3Service;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private static final long MAX_FILE_SIZE = 10_485_760L; // 10MB

    private final S3Service s3Service;
    private final FileRepository fileRepository;
    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final ChatService chatService;

    /**
     * Presigned Upload URL 생성
     * POST /api/files/presigned-url
     */
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> generatePresignedUrl(
            @Valid @RequestBody PresignedUrlRequest request
    ) {
        logger.info("Generating presigned URL for file: {}", request.fileName());

        // 1. 파일 크기 검증
        if (request.fileSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "File size exceeds 10MB limit");
        }

        // 2. 확장자 추출 및 검증
        String extension = extractExtension(request.fileName());
        try {
            MessageTypeDetector.isAllowedExtension(extension);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
        }

        // 3. Content-Type과 확장자 일치 확인
        if (!MessageTypeDetector.matchesContentType(extension, request.contentType())) {
            logger.warn("Content-Type mismatch: extension={}, contentType={}", extension, request.contentType());
        }

        // 4. UUID 생성 및 파일 키 구성
        String uuid = UuidCreator.getTimeOrderedEpoch().toString();
        String fileKey = request.directory() + "/" + uuid;

        // 5. Presigned URL 생성
        String uploadUrl = s3Service.generatePresignedUploadUrl(fileKey, request.contentType());

        logger.info("Generated presigned URL for fileKey: {}", fileKey);

        return ResponseEntity.ok(new PresignedUrlResponse(
                uploadUrl,
                fileKey,
                request.fileName(),
                300  // 5분
        ));
    }

    /**
     * 파일 업로드 확인 및 메시지 생성
     * POST /api/files/confirm
     */
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, Object>> confirmUpload(
            @Valid @RequestBody FileConfirmRequest request
    ) {
        logger.info("Confirming upload for fileKey: {}", request.fileKey());

        // 1. S3 파일 존재 및 메타데이터 확인
        HeadObjectResponse s3Metadata = s3Service.getFileMetadata(request.fileKey());
        long actualSize = s3Metadata.contentLength();
        String contentType = s3Metadata.contentType();

        // 2. 파일 크기 재검증
        if (actualSize > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Uploaded file size exceeds 10MB");
        }

        // 3. 확장자 추출 및 검증
        String extension = extractExtension(request.fileName());
        String nameWithoutExtension = removeExtension(request.fileName());

        // 4. File 엔티티 생성
        File file = File.builder()
                .name(nameWithoutExtension)
                .extension(extension)
                .size(actualSize)
                .path(request.fileKey())
                .build();
        file = fileRepository.save(file);

        // 5. 메시지 타입 판별
        MessageType messageType = MessageTypeDetector.determineType(null, extension);

        // 6. Channel 및 WorkspaceUser 조회
        Channel channel = channelRepository.findById(request.channelId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        WorkspaceUser workspaceUser = workspaceUserRepository.findById(request.workspaceUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        // 7. Message 생성 (content는 null, DOCUMENT 타입만 추후 요약 생성)
        Message message = Message.builder()
                .channel(channel)
                .workspaceUser(workspaceUser)
                .type(messageType)
                .file(file)
                .content(null)  // 파일 업로드 직후에는 content 없음
                .build();

        // 8. 메시지 저장 및 브로드캐스트
        chatService.sendMessage(message);

        logger.info("File upload confirmed. messageId={}, fileId={}, type={}", 
                message.getId(), file.getId(), messageType);

        // 9. 응답
        Map<String, Object> response = new HashMap<>();
        response.put("messageId", message.getId());
        response.put("fileId", file.getId());
        response.put("type", messageType.name());
        response.put("fileUrl", s3Service.getFileUrl(request.fileKey()));
        response.put("createdAt", message.getCreatedAt());

        return ResponseEntity.ok(response);
    }

    /**
     * 파일명에서 확장자 추출
     */
    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "File must have an extension");
        }
        int lastDot = fileName.lastIndexOf(".");
        return fileName.substring(lastDot + 1).toLowerCase();
    }

    /**
     * 파일명에서 확장자 제거
     */
    private String removeExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return fileName;
        }
        int lastDot = fileName.lastIndexOf(".");
        return fileName.substring(0, lastDot);
    }
}
