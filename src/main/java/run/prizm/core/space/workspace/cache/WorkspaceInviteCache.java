package run.prizm.core.space.workspace.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceInviteCache implements Serializable {

    private String code;
    private Long workspaceId;
    private Long allowedUserId;
    private Long expiresAt;
    private Integer maxUses;
    private Integer usageCount;
    private Long createdAt;
    
    private WorkspaceUserRole role;
    private List<Long> allowedUserIds;
    private List<Long> autoJoinGroupIds;
    private Long channelId;

    public WorkspaceInviteCache(String code, Long workspaceId, Long allowedUserId, 
                                Long expiresAt, Integer maxUses, Integer usageCount, Long createdAt) {
        this.code = code;
        this.workspaceId = workspaceId;
        this.allowedUserId = allowedUserId;
        this.expiresAt = expiresAt;
        this.maxUses = maxUses;
        this.usageCount = usageCount;
        this.createdAt = createdAt;
    }

    public boolean isExpired() {
        if (expiresAt == null) {
            return false;
        }
        return Instant.now().toEpochMilli() >= expiresAt;
    }

    public boolean hasReachedMaxUses() {
        return maxUses != null && usageCount != null && usageCount >= maxUses;
    }

    public void incrementUsage() {
        if (usageCount == null) {
            usageCount = 0;
        }
        usageCount++;
    }
}