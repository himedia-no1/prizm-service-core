package run.prizm.core.user.dto;

import run.prizm.core.user.constraint.UserNotifyType;
import java.time.Instant;
import java.util.List;

public record UserNotifyListResponse(
        List<UserNotifyItem> notifications
) {
    public record UserNotifyItem(
            Long id,
            UserNotifyType type,
            Long senderId,
            String content,
            Long locationId,
            boolean important,
            boolean read,
            Instant createdAt
    ) {}
}
