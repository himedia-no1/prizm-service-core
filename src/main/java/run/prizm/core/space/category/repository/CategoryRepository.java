package run.prizm.core.space.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import run.prizm.core.space.category.entity.Category;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    @Query("SELECT c FROM Category c WHERE c.workspace.id = :workspaceId AND c.deletedAt IS NULL ORDER BY c.zIndex ASC")
    List<Category> findByWorkspaceIdAndDeletedAtIsNullOrderByZIndex(Long workspaceId);
    
    @Query("SELECT c FROM Category c WHERE c.workspace.id = :workspaceId AND c.deletedAt IS NULL ORDER BY c.zIndex ASC")
    Optional<Category> findFirstByWorkspaceId(Long workspaceId);
    
    @Query("SELECT c FROM Category c WHERE c.workspace.id = :workspaceId AND c.deletedAt IS NULL ORDER BY c.zIndex DESC")
    Optional<Category> findLastByWorkspaceId(Long workspaceId);
}