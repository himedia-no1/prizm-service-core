package run.prizm.core.message.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.message.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
}
