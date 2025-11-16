package run.prizm.core.space.channel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.space.channel.entity.ChannelWorkspaceUser;

public interface ChannelWorkspaceUserRepository extends JpaRepository<ChannelWorkspaceUser, Long> {
}