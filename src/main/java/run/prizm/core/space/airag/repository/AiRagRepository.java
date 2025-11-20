package run.prizm.core.space.airag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.space.airag.entity.AiRag;

import java.util.List;
import java.util.Optional;

public interface AiRagRepository extends JpaRepository<AiRag, Long> {
    
    List<AiRag> findByWorkspaceIdAndDeletedAtIsNull(Long workspaceId);
    
    Optional<AiRag> findByFileId(Long fileId);
}