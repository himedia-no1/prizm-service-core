package run.prizm.core.message.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import run.prizm.core.message.analysis.DocumentAnalysisService;
import run.prizm.core.message.dto.AIChatRequest;
import run.prizm.core.message.dto.AIChatResponse;
import run.prizm.core.message.dto.MessageResponse;
import run.prizm.core.message.entity.Message;
import run.prizm.core.message.repository.MessageRepository;
import run.prizm.core.message.service.AIAssistantService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);
    private final DocumentAnalysisService documentAnalysisService;
    private final AIAssistantService aiAssistantService;
    private final MessageRepository messageRepository;

    /**
     * 채널의 메시지 목록 조회
     * GET /api/messages?channelId={channelId}&limit={limit}
     */
    @GetMapping
    public ResponseEntity<List<MessageResponse>> getMessages(
            @RequestParam Long channelId,
            @RequestParam(defaultValue = "50") int limit) {
        logger.info("Fetching messages for channelId: {}, limit: {}", channelId, limit);
        
        List<Message> messages = messageRepository.findByChannelIdOrderByCreatedAtDesc(
                channelId, 
                PageRequest.of(0, limit));
        
        List<MessageResponse> response = messages.stream()
                .map(MessageResponse::from)
                .collect(Collectors.toList());
        
        // 최신 순으로 정렬 (DB에서 DESC로 가져왔으므로 reverse)
        java.util.Collections.reverse(response);
        
        logger.info("Found {} messages for channelId: {}", response.size(), channelId);
        return ResponseEntity.ok(response);
    }

    /**
     * 문서 분석 요청
     * POST /api/messages/{messageId}/analyze
     */
    @PostMapping("/{messageId}/analyze")
    public ResponseEntity<Map<String, Object>> analyzeDocument(@PathVariable Long messageId) {
        logger.info("Received document analysis request for messageId: {}", messageId);

        // 비동기 분석 시작
        documentAnalysisService.analyzeDocument(messageId);

        // 즉시 응답
        return ResponseEntity.accepted()
                .body(Map.of(
                        "status", "processing",
                        "messageId", messageId,
                        "message", "Document analysis started"
                ));
    }

    /**
     * AI 어시스턴트 채팅
     * POST /api/messages/ai-chat
     */
    @PostMapping("/ai-chat")
    public ResponseEntity<AIChatResponse> aiChat(@Valid @RequestBody AIChatRequest request) {
        logger.info("Received AI chat request: workspaceUserId={}, query={}", 
                request.getWorkspaceUserId(), request.getQuery());

        AIChatResponse response = aiAssistantService.chat(request);

        return ResponseEntity.ok(response);
    }
}
