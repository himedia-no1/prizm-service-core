package run.prizm.core.space.channel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.space.channel.entity.ChannelWebhook;

public interface ChannelWebhookRepository extends JpaRepository<ChannelWebhook, Long> {
}
