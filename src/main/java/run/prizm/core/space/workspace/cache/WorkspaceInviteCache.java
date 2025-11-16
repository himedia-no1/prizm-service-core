package run.prizm.core.space.workspace.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkspaceInviteCache implements Serializable {

    private String code;
    private Long workspaceId;
    private Long allowedUserId;
    private Long expiresAtEpochMillis;
    private Integer maxUses;
    private Integer usedCount;
    private Long createdAtEpochMillis;

    public boolean isExpired() {
        if (expiresAtEpochMillis == null) {
            return false;
        }
        return Instant.now().toEpochMilli() >= expiresAtEpochMillis;
    }

    public boolean hasUsageLimit() {
        return maxUses != null && maxUses > 0;
    }

    public boolean hasReachedMaxUses() {
        return hasUsageLimit() && usedCount != null && usedCount >= maxUses;
    }

    public Instant expiresAt() {
        return expiresAtEpochMillis == null ? null : Instant.ofEpochMilli(expiresAtEpochMillis);
    }

    public void incrementUsage() {
        if (usedCount == null) {
            usedCount = 0;
        }
        usedCount++;
    }
}