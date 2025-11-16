package run.prizm.core.space.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.space.group.entity.Group;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    
    List<Group> findByWorkspaceIdAndDeletedAtIsNullOrderByName(Long workspaceId);
}