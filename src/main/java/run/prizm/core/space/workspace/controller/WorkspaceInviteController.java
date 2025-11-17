package run.prizm.core.space.workspace.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import run.prizm.core.security.permission.RequireWorkspaceRole;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.dto.*;
import run.prizm.core.space.workspace.service.WorkspaceInviteService;
import run.prizm.core.user.resolver.CurrentUser;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WorkspaceInviteController {

    private final WorkspaceInviteService workspaceInviteService;

    @PostMapping("/api/workspaces/{workspaceId}/invites")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER, WorkspaceUserRole.MEMBER})
    public ResponseEntity<WorkspaceInviteCreateResponse> createInvite(
            @PathVariable Long workspaceId,
            @CurrentUser Long userId,
            @Valid @RequestBody WorkspaceInviteCreateRequest request
    ) {
        WorkspaceInviteCreateResponse response;
        
        if (request.channelId() != null) {
            response = workspaceInviteService.createGuestInvite(
                    workspaceId, 
                    userId, 
                    request.channelId(), 
                    request.allowedUserIds()
            );
        } else {
            response = workspaceInviteService.createMemberInvite(workspaceId, userId, request);
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/workspaces/{workspaceId}/invites")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<List<WorkspaceInviteInfoResponse>> getInviteList(
            @PathVariable Long workspaceId
    ) {
        List<WorkspaceInviteInfoResponse> response = workspaceInviteService.getInviteList(workspaceId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/workspaces/{workspaceId}/invites/{code}")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Void> deleteInvite(
            @PathVariable Long workspaceId,
            @PathVariable String code
    ) {
        workspaceInviteService.deleteInvite(workspaceId, code);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/invites/{code}/join")
    public ResponseEntity<WorkspaceInviteJoinResponse> joinByInvite(
            @PathVariable String code,
            @CurrentUser Long userId
    ) {
        WorkspaceInviteJoinResponse response = workspaceInviteService.joinByInvite(code, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/invites/{code}")
    public ResponseEntity<WorkspaceResponse> getWorkspaceByInviteCode(
            @PathVariable String code
    ) {
        WorkspaceResponse response = workspaceInviteService.getWorkspaceByInviteCode(code);
        return ResponseEntity.ok(response);
    }
}
