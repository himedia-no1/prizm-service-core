package run.prizm.core.space.workspace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.space.workspace.entity.WorkspaceUser;

public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, Long> {

    boolean existsByWorkspaceIdAndUserIdAndDeletedAtIsNull(Long workspaceId, Long userId);

    long countByWorkspaceIdAndDeletedAtIsNull(Long workspaceId);
}