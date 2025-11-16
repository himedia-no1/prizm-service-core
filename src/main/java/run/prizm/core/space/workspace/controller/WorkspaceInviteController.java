package run.prizm.core.space.workspace.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.resolver.CurrentUser;
import run.prizm.core.space.workspace.dto.WorkspaceInviteCreateRequest;
import run.prizm.core.space.workspace.dto.WorkspaceInviteCreateResponse;
import run.prizm.core.space.workspace.service.WorkspaceInviteService;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/invite")
@RequiredArgsConstructor
public class WorkspaceInviteController {

    private final WorkspaceInviteService workspaceInviteService;

    @PostMapping
    public ResponseEntity<WorkspaceInviteCreateResponse> createInvite(
            @PathVariable Long workspaceId,
            @CurrentUser User user,
            @Valid @RequestBody WorkspaceInviteCreateRequest request
    ) {
        WorkspaceInviteCreateResponse response = workspaceInviteService.createInvite(workspaceId, user.getId(), request);
        return ResponseEntity.ok(response);
    }
}