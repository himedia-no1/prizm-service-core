package run.prizm.core.space.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.common.util.ZIndexCalculator;
import run.prizm.core.space.category.dto.CategoryCreateRequest;
import run.prizm.core.space.category.dto.CategoryResponse;
import run.prizm.core.space.category.dto.CategoryUpdateRequest;
import run.prizm.core.space.category.dto.CategoryZIndexUpdateRequest;
import run.prizm.core.space.category.entity.Category;
import run.prizm.core.space.category.repository.CategoryRepository;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.repository.WorkspaceRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public CategoryResponse createCategory(Long workspaceId, CategoryCreateRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                                                 .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_NOT_FOUND));

        BigDecimal zIndex = categoryRepository.findLastByWorkspaceId(workspaceId)
                                              .map(lastCategory -> lastCategory.getZIndex()
                                                                               .add(BigDecimal.ONE))
                                              .orElse(BigDecimal.ONE);

        Category category = Category.builder()
                                    .workspace(workspace)
                                    .name(request.name())
                                    .zIndex(zIndex)
                                    .build();

        category = categoryRepository.save(category);

        return toResponse(category);
    }

    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                                              .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        if (request.name() != null) {
            category.setName(request.name());
        }

        category = categoryRepository.save(category);

        return toResponse(category);
    }

    @Transactional
    public void updateZIndex(Long categoryId, CategoryZIndexUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                                              .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        List<Category> categories = categoryRepository
                .findByWorkspaceIdAndDeletedAtIsNullOrderByZIndex(category.getWorkspace()
                                                                          .getId());

        BigDecimal newZIndex;

        if ("FIRST".equals(request.position())) {
            Category firstCategory = categories.get(0);
            newZIndex = ZIndexCalculator.calculateFirst(firstCategory.getZIndex());
        } else if ("LAST".equals(request.position())) {
            Category lastCategory = categories.get(categories.size() - 1);
            newZIndex = ZIndexCalculator.calculateLast(lastCategory.getZIndex());
        } else if ("BETWEEN".equals(request.position())) {
            Category beforeCategory = categoryRepository.findById(request.beforeId())
                                                        .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
            Category afterCategory = categoryRepository.findById(request.afterId())
                                                       .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));
            newZIndex = ZIndexCalculator.calculateBetween(beforeCategory.getZIndex(), afterCategory.getZIndex());
        } else {
            throw new BusinessException(ErrorCode.INVALID_POSITION);
        }

        category.setZIndex(newZIndex);
        categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                                              .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        category.setDeletedAt(Instant.now());
        categoryRepository.save(category);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getWorkspace()
                        .getId(),
                category.getName(),
                category.getZIndex()
                        .toPlainString(),
                category.getCreatedAt()
        );
    }
}