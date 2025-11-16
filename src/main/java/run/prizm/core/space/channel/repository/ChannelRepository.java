package run.prizm.core.space.channel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.space.channel.entity.Channel;

public interface ChannelRepository extends JpaRepository<Channel, Long> {
}