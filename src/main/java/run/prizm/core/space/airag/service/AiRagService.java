package run.prizm.core.space.airag.service;

import com.github.f4b6a3.uuid.UuidCreator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.file.entity.File;
import run.prizm.core.file.repository.FileRepository;
import run.prizm.core.properties.UrlProperties;
import run.prizm.core.space.airag.constraint.AiRagProgress;
import run.prizm.core.space.airag.dto.*;
import run.prizm.core.space.airag.entity.AiRag;
import run.prizm.core.space.airag.permission.AiRagPermissionChecker;
import run.prizm.core.space.airag.repository.AiRagRepository;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.repository.WorkspaceRepository;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;
import run.prizm.core.storage.s3.S3Service;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiRagService {

    private static final Logger logger = LoggerFactory.getLogger(AiRagService.class);
    private static final long MAX_FILE_SIZE = 50_485_760L; // 50MB for RAG files

    private final AiRagRepository aiRagRepository;
    private final FileRepository fileRepository;
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final S3Service s3Service;
    private final WebClient.Builder webClientBuilder;
    private final AiRagPermissionChecker permissionChecker;
    private final UrlProperties urlProperties;

    /**
     * Presigned Upload URL 생성
     */
    public AiRagUploadResponse generatePresignedUrl(AiRagUploadRequest request) {
        logger.info("Generating presigned URL for RAG file: {}", request.getFileName());

        // 0. 권한 체크 (OWNER, MANAGER만 가능)
        permissionChecker.checkAiRagManagePermission(request.getWorkspaceUserId());

        // 1. 파일 크기 검증
        if (request.getFileSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "File size exceeds 50MB limit");
        }

        // 2. 확장자 검증 (RAG 지원 파일만)
        String extension = extractExtension(request.getFileName());
        validateRagFileExtension(extension);

        // 3. UUID 생성 및 파일 키 구성
        String uuid = UuidCreator.getTimeOrderedEpoch()
                                 .toString();
        String fileKey = "rag/" + uuid;

        // 4. Presigned URL 생성
        String uploadUrl = s3Service.generatePresignedUploadUrl(fileKey, request.getContentType());

        logger.info("Generated presigned URL for fileKey: {}", fileKey);

        return new AiRagUploadResponse(uploadUrl, fileKey, request.getFileName(), 300);
    }

    /**
     * 파일 업로드 확인 및 RAG 처리 시작
     */
    @Transactional
    public AiRagResponse confirmUpload(AiRagConfirmRequest request) {
        logger.info("Confirming RAG file upload: {}", request.getFileKey());

        // 0. 권한 체크 (OWNER, MANAGER만 가능)
        permissionChecker.checkAiRagManagePermission(request.getWorkspaceUserId());

        // 1. S3 파일 존재 및 메타데이터 확인
        HeadObjectResponse s3Metadata = s3Service.getFileMetadata(request.getFileKey());
        long actualSize = s3Metadata.contentLength();

        // 2. 파일 크기 재검증
        if (actualSize > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Uploaded file size exceeds 50MB");
        }

        // 3. 확장자 추출
        String extension = extractExtension(request.getFileName());
        String nameWithoutExtension = removeExtension(request.getFileName());

        // 4. File 엔티티 생성
        File file = File.builder()
                        .name(nameWithoutExtension)
                        .extension(extension)
                        .size(actualSize)
                        .path(request.getFileKey())
                        .build();
        file = fileRepository.save(file);

        // 5. Workspace 및 WorkspaceUser 조회
        Workspace workspace = workspaceRepository.findById(request.getWorkspaceId())
                                                 .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_NOT_FOUND));

        WorkspaceUser workspaceUser = workspaceUserRepository.findById(request.getWorkspaceUserId())
                                                             .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        // 6. AiRag 엔티티 생성 (상태: IN_PROGRESS)
        AiRag aiRag = AiRag.builder()
                           .workspace(workspace)
                           .workspaceUser(workspaceUser)
                           .file(file)
                           .progress(AiRagProgress.IN_PROGRESS)
                           .build();
        aiRag = aiRagRepository.save(aiRag);

        logger.info("AiRag created: id={}, fileId={}", aiRag.getId(), file.getId());

        // 7. FastAPI에 비동기로 RAG 처리 요청
        requestRagProcessing(aiRag);

        // 8. 응답 생성
        return convertToResponse(aiRag);
    }

    /**
     * FastAPI에 RAG 처리 요청 (비동기)
     */
    @Async
    public CompletableFuture<Void> requestRagProcessing(AiRag aiRag) {
        logger.info("Requesting RAG processing to FastAPI for aiRagId={}", aiRag.getId());

        try {
            WebClient webClient = webClientBuilder.baseUrl(urlProperties.getServiceAiUrl())
                                                  .build();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("workspace_id", aiRag.getWorkspace()
                                                 .getId());
            requestBody.put("file_id", aiRag.getFile()
                                            .getId());
            requestBody.put("file_key", aiRag.getFile()
                                             .getPath());
            requestBody.put("file_name", aiRag.getFile()
                                              .getName() + "." + aiRag.getFile()
                                                                      .getExtension());
            requestBody.put("callback_url", urlProperties.getServiceCoreUrl() + "/api/ai-rag/callback");

            webClient.post()
                     .uri("/ai/rag")
                     .bodyValue(requestBody)
                     .retrieve()
                     .bodyToMono(Map.class)
                     .subscribe(
                             response -> logger.info("RAG processing request sent successfully: {}", response),
                             error -> logger.error("Failed to request RAG processing: {}", error.getMessage())
                     );

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            logger.error("Failed to request RAG processing", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * FastAPI 콜백 처리
     */
    @Transactional
    public void handleCallback(AiRagCallbackRequest request) {
        logger.info("Received callback from FastAPI: fileId={}, status={}",
                request.getFileId(), request.getStatus());

        // File ID로 AiRag 조회
        AiRag aiRag = aiRagRepository.findByFileId(request.getFileId())
                                     .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "AiRag not found"));

        // 상태 업데이트
        if ("SUCCESS".equals(request.getStatus())) {
            aiRag.setProgress(AiRagProgress.SUCCESS);
            logger.info("RAG processing completed successfully: chunks={}, vectors={}",
                    request.getChunksCount(), request.getVectorsCount());
        } else {
            aiRag.setProgress(AiRagProgress.FAILURE);
            logger.error("RAG processing failed: {}", request.getError());
        }

        aiRagRepository.save(aiRag);
    }

    /**
     * 워크스페이스의 모든 RAG 파일 조회
     */
    @Transactional(readOnly = true)
    public List<AiRagResponse> getWorkspaceRagFiles(Long workspaceId) {
        List<AiRag> ragFiles = aiRagRepository.findByWorkspaceIdAndDeletedAtIsNull(workspaceId);
        return ragFiles.stream()
                       .map(this::convertToResponse)
                       .collect(Collectors.toList());
    }

    /**
     * RAG 파일 삭제 (Soft Delete + Qdrant 벡터 삭제)
     */
    @Transactional
    public void deleteRagFile(Long ragId, Long workspaceId, Long workspaceUserId) {
        // 0. 권한 체크 (OWNER, MANAGER만 가능)
        permissionChecker.checkAiRagManagePermission(workspaceUserId);

        AiRag aiRag = aiRagRepository.findById(ragId)
                                     .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "RAG file not found"));

        // 권한 확인
        if (!aiRag.getWorkspace()
                  .getId()
                  .equals(workspaceId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // FastAPI에 벡터 삭제 요청
        deleteVectorsFromQdrant(aiRag);

        // Soft Delete
        aiRag.setDeletedAt(java.time.Instant.now());
        aiRagRepository.save(aiRag);

        logger.info("RAG file deleted: id={}, fileId={}", ragId, aiRag.getFile()
                                                                      .getId());
    }

    /**
     * Qdrant 벡터 삭제 요청
     */
    @Async
    public CompletableFuture<Void> deleteVectorsFromQdrant(AiRag aiRag) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(urlProperties.getServiceAiUrl())
                                                  .build();

            String uri = String.format("/ai/rag/workspaces/%d/files/%d", 
                    aiRag.getWorkspace().getId(), 
                    aiRag.getFile().getId());

            webClient.delete()
                     .uri(uri)
                     .retrieve()
                     .bodyToMono(Map.class)
                     .subscribe(
                             response -> logger.info("Vectors deleted from Qdrant: {}", response),
                             error -> logger.error("Failed to delete vectors: {}", error.getMessage())
                     );

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            logger.error("Failed to request vector deletion", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "File must have an extension");
        }
        int lastDot = fileName.lastIndexOf(".");
        return fileName.substring(lastDot + 1)
                       .toLowerCase();
    }

    private String removeExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return fileName;
        }
        int lastDot = fileName.lastIndexOf(".");
        return fileName.substring(0, lastDot);
    }

    private void validateRagFileExtension(String extension) {
        List<String> allowedExtensions = List.of("pdf", "docx", "txt", "pptx", "xlsx");
        if (!allowedExtensions.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,
                    "Unsupported file type for RAG. Allowed: " + String.join(", ", allowedExtensions));
        }
    }

    private AiRagResponse convertToResponse(AiRag aiRag) {
        AiRagResponse response = new AiRagResponse();
        response.setId(aiRag.getId());
        response.setWorkspaceId(aiRag.getWorkspace()
                                     .getId());
        response.setFileId(aiRag.getFile()
                                .getId());
        response.setFileName(aiRag.getFile()
                                  .getName());
        response.setFileExtension(aiRag.getFile()
                                       .getExtension());
        response.setFileSize(aiRag.getFile()
                                  .getSize());
        response.setFileUrl(s3Service.getFileUrl(aiRag.getFile()
                                                      .getPath()));
        response.setProgress(aiRag.getProgress());
        response.setCreatedAt(aiRag.getCreatedAt());
        response.setUpdatedAt(aiRag.getUpdatedAt());
        return response;
    }
}