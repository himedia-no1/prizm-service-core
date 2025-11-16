package run.prizm.core.space.category.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.space.category.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}