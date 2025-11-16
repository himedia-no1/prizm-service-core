package run.prizm.core.space.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.space.group.entity.GroupChannel;

public interface GroupChannelRepository extends JpaRepository<GroupChannel, Long> {
}