package run.prizm.core.space.airag.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import run.prizm.core.space.airag.dto.*;
import run.prizm.core.space.airag.service.AiRagService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai-rag")
@RequiredArgsConstructor
public class AiRagController {

    private final AiRagService aiRagService;

    /**
     * RAG 파일 업로드용 Presigned URL 생성
     * POST /api/ai-rag/presigned-url
     */
    @PostMapping("/presigned-url")
    public ResponseEntity<AiRagUploadResponse> generatePresignedUrl(
            @Valid @RequestBody AiRagUploadRequest request
    ) {
        AiRagUploadResponse response = aiRagService.generatePresignedUrl(request);
        return ResponseEntity.ok(response);
    }

    /**
     * RAG 파일 업로드 확인 및 처리 시작
     * POST /api/ai-rag/confirm
     */
    @PostMapping("/confirm")
    public ResponseEntity<AiRagResponse> confirmUpload(
            @Valid @RequestBody AiRagConfirmRequest request
    ) {
        AiRagResponse response = aiRagService.confirmUpload(request);
        return ResponseEntity.ok(response);
    }

    /**
     * FastAPI 콜백 수신
     * POST /api/ai-rag/callback
     */
    @PostMapping("/callback")
    public ResponseEntity<Map<String, String>> handleCallback(
            @RequestBody AiRagCallbackRequest request
    ) {
        aiRagService.handleCallback(request);
        return ResponseEntity.ok(Map.of("status", "received"));
    }

    /**
     * 워크스페이스의 RAG 파일 목록 조회
     * GET /api/ai-rag/workspace/{workspaceId}
     */
    @GetMapping("/workspace/{workspaceId}")
    public ResponseEntity<List<AiRagResponse>> getWorkspaceRagFiles(
            @PathVariable Long workspaceId
    ) {
        List<AiRagResponse> files = aiRagService.getWorkspaceRagFiles(workspaceId);
        return ResponseEntity.ok(files);
    }

    /**
     * RAG 파일 삭제
     * DELETE /api/ai-rag/{ragId}
     */
    @DeleteMapping("/{ragId}")
    public ResponseEntity<Map<String, String>> deleteRagFile(
            @PathVariable Long ragId,
            @RequestParam Long workspaceId,
            @RequestParam Long workspaceUserId
    ) {
        aiRagService.deleteRagFile(ragId, workspaceId, workspaceUserId);
        return ResponseEntity.ok(Map.of("status", "deleted"));
    }
}
