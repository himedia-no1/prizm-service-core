package run.prizm.core.space.workspace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.space.workspace.entity.Workspace;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
}