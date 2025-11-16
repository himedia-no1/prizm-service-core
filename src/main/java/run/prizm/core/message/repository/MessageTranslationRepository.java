package run.prizm.core.message.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.message.entity.MessageTranslation;

public interface MessageTranslationRepository extends JpaRepository<MessageTranslation, Long> {
}
