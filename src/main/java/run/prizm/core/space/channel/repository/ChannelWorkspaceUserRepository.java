package run.prizm.core.space.channel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.entity.ChannelWorkspaceUser;
import run.prizm.core.space.workspace.entity.WorkspaceUser;

import java.util.List;
import java.util.Optional;

public interface ChannelWorkspaceUserRepository extends JpaRepository<ChannelWorkspaceUser, Long> {

    Optional<ChannelWorkspaceUser> findByChannelIdAndWorkspaceUserId(Long channelId, Long workspaceUserId);

    Optional<ChannelWorkspaceUser> findByChannelAndWorkspaceUser(Channel channel, WorkspaceUser workspaceUser);

    List<ChannelWorkspaceUser> findByWorkspaceUserAndExplicitTrue(WorkspaceUser workspaceUser);
}