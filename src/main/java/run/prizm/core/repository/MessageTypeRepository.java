package run.prizm.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.message.constraint.MessageType;

public interface MessageTypeRepository extends JpaRepository<MessageType, String> {
}
