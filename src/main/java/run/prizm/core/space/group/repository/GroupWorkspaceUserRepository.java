package run.prizm.core.space.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.space.group.entity.GroupWorkspaceUser;

public interface GroupWorkspaceUserRepository extends JpaRepository<GroupWorkspaceUser, Long> {
}