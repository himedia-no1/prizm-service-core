package run.prizm.core.space.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.prizm.core.space.category.dto.CategoryCreateRequest;
import run.prizm.core.space.category.entity.Category;
import run.prizm.core.space.category.service.CategoryService;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Category> createCategory(
            @PathVariable Long workspaceId,
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        Category category = categoryService.createCategory(workspaceId, request);
        return ResponseEntity.ok(category);
    }
}