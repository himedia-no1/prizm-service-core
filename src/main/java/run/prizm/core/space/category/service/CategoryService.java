package run.prizm.core.space.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.space.category.dto.CategoryCreateRequest;
import run.prizm.core.space.category.entity.Category;
import run.prizm.core.space.category.repository.CategoryRepository;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.repository.WorkspaceRepository;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public Category createCategory(Long workspaceId, CategoryCreateRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                                                 .orElseThrow(() -> new RuntimeException("Workspace not found"));

        Category category = Category.builder()
                                    .workspace(workspace)
                                    .name(request.name())
                                    .zIndex(0D)
                                    .build();

        return categoryRepository.save(category);
    }
}