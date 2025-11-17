package run.prizm.core.space.workspace.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import run.prizm.core.security.permission.RequireWorkspaceRole;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.dto.WorkspaceCreateRequest;
import run.prizm.core.space.workspace.dto.WorkspaceListItemResponse;
import run.prizm.core.space.workspace.dto.WorkspaceResponse;
import run.prizm.core.space.workspace.dto.WorkspaceUpdateRequest;
import run.prizm.core.space.workspace.service.WorkspaceService;
import run.prizm.core.user.resolver.CurrentUser;

import java.util.List;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @GetMapping
    public ResponseEntity<List<WorkspaceListItemResponse>> listUserWorkspaces(
            @CurrentUser Long userId
    ) {
        List<WorkspaceListItemResponse> response = workspaceService.listUserWorkspaces(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @CurrentUser Long userId,
            @Valid @RequestBody WorkspaceCreateRequest request
    ) {
        WorkspaceResponse response = workspaceService.createWorkspace(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponse> getWorkspace(
            @PathVariable Long workspaceId
    ) {
        WorkspaceResponse response = workspaceService.getWorkspace(workspaceId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{workspaceId}")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<WorkspaceResponse> updateWorkspace(
            @PathVariable Long workspaceId,
            @Valid @ModelAttribute WorkspaceUpdateRequest request
    ) {
        WorkspaceResponse response = workspaceService.updateWorkspace(workspaceId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{workspaceId}")
    @RequireWorkspaceRole(WorkspaceUserRole.OWNER)
    public ResponseEntity<Void> deleteWorkspace(
            @PathVariable Long workspaceId
    ) {
        workspaceService.deleteWorkspace(workspaceId);
        return ResponseEntity.noContent()
                             .build();
    }
}