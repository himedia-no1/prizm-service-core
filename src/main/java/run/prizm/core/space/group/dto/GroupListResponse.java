package run.prizm.core.space.group.dto;

import java.util.List;

public record GroupListResponse(
        List<GroupItem> groups
) {
    public record GroupItem(
            Long id,
            String name
    ) {}
}
