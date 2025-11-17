package run.prizm.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.message.entity.MessageTranslation;

import java.util.Optional;

public interface MessageTranslationRepository extends JpaRepository<MessageTranslation, Long> {
    Optional<MessageTranslation> findByMessageIdAndLanguageCode(Long messageId, String languageCode);
}
