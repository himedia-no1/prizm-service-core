package run.prizm.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.message.entity.MessageEmoji;

public interface MessageEmojiRepository extends JpaRepository<MessageEmoji, Long> {
}
