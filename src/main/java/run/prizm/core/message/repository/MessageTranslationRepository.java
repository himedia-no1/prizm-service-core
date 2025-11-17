package run.prizm.core.message.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.message.entity.MessageTranslation;

import java.util.Optional;

public interface MessageTranslationRepository extends JpaRepository<MessageTranslation, Long> {
    Optional<MessageTranslation> findByMessageIdAndLanguage(Long message_id, Language language);
}