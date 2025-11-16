package run.prizm.core.space.workspace.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.resolver.CurrentUser;
import run.prizm.core.space.workspace.dto.WorkspaceCreateRequest;
import run.prizm.core.space.workspace.dto.WorkspaceResponse;
import run.prizm.core.space.workspace.service.WorkspaceService;

@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @PostMapping
    public ResponseEntity<WorkspaceResponse> createWorkspace(
            @CurrentUser User user,
            @Valid @RequestBody WorkspaceCreateRequest request
    ) {
        WorkspaceResponse response = workspaceService.createWorkspace(user.getId(), request);
        return ResponseEntity.ok(response);
    }
}