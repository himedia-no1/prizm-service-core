package run.prizm.core.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.common.constraint.Language;

public interface LanguageRepository extends JpaRepository<Language, String> {
}
