package run.prizm.core.message.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import run.prizm.core.message.constraint.MessageType;
import run.prizm.core.message.entity.Message;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageResponse {
    
    private Long id;
    private Long channelId;
    private Long workspaceUserId;
    private String userId;
    private String username;
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
            if (message.getWorkspaceUser().getUser() != null) {
                builder.userId(String.valueOf(message.getWorkspaceUser().getUser().getId()));
                builder.username(message.getWorkspaceUser().getUser().getName());
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
