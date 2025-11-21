package run.prizm.core.message.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import run.prizm.core.message.constraint.MessageType;
import run.prizm.core.message.entity.Message;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {
    
    private Long id;
    private Long channelId;
    private Long workspaceUserId;
    private String userId;
    private String username;
    private String userAvatar;  // 워크스페이스 유저 아바타 추가
    private MessageType type;
    private String content;
    private boolean edited;
    private boolean pinned;
    private Long fileId;
    private Long replyToId;
    private Long threadId;
    private Instant createdAt;
    private Instant updatedAt;
    
    public static MessageResponse from(Message message) {
        MessageResponseBuilder builder = MessageResponse.builder()
                .id(message.getId())
                .channelId(message.getChannel().getId())
                .type(message.getType())
                .content(message.getContent())
                .edited(message.isEdited())
                .pinned(message.isPinned())
                .createdAt(message.getCreatedAt())
                .updatedAt(message.getUpdatedAt());
        
        if (message.getWorkspaceUser() != null) {
            builder.workspaceUserId(message.getWorkspaceUser().getId());
            
            // WorkspaceUser의 이름 사용 (워크스페이스마다 다를 수 있음)
            if (message.getWorkspaceUser().getName() != null) {
                builder.username(message.getWorkspaceUser().getName());
            } else if (message.getWorkspaceUser().getUser() != null) {
                // WorkspaceUser 이름이 없으면 전역 User 이름 사용 (fallback)
                builder.username(message.getWorkspaceUser().getUser().getName());
            }
            
            // WorkspaceUser의 아바타 이미지
            if (message.getWorkspaceUser().getImage() != null) {
                builder.userAvatar(message.getWorkspaceUser().getImage().getPath());
            }
            
            // 전역 userId도 함께 전송
            if (message.getWorkspaceUser().getUser() != null) {
                builder.userId(String.valueOf(message.getWorkspaceUser().getUser().getId()));
            }
        }
        
        if (message.getFile() != null) {
            builder.fileId(message.getFile().getId());
        }
        
        if (message.getReply() != null) {
            builder.replyToId(message.getReply().getId());
        }
        
        if (message.getThread() != null) {
            builder.threadId(message.getThread().getId());
        }
        
        return builder.build();
    }
}
