package run.prizm.core.space.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import run.prizm.core.security.permission.RequireWorkspaceRole;
import run.prizm.core.space.category.dto.CategoryCreateRequest;
import run.prizm.core.space.category.dto.CategoryResponse;
import run.prizm.core.space.category.dto.CategoryUpdateRequest;
import run.prizm.core.space.category.dto.CategoryZIndexUpdateRequest;
import run.prizm.core.space.category.service.CategoryService;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<CategoryResponse> createCategory(
            @PathVariable Long workspaceId,
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        CategoryResponse response = categoryService.createCategory(workspaceId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{categoryId}")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long categoryId,
            @RequestBody CategoryUpdateRequest request
    ) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{categoryId}/z-index")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Void> updateZIndex(
            @PathVariable Long categoryId,
            @RequestBody CategoryZIndexUpdateRequest request
    ) {
        categoryService.updateZIndex(categoryId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{categoryId}")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Long categoryId
    ) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.noContent().build();
    }
}
