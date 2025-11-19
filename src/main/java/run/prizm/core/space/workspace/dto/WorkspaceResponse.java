package run.prizm.core.space.workspace.dto;

import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;

import java.time.Instant;

public record WorkspaceResponse(
        Long id,
        String name,
        String imageUrl,
        Instant createdAt,
        WorkspaceUserRole myRole
) {
}