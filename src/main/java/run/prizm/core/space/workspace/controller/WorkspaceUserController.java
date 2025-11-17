package run.prizm.core.space.workspace.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import run.prizm.core.security.permission.RequireWorkspaceRole;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.dto.*;
import run.prizm.core.space.workspace.service.WorkspaceUserService;
import run.prizm.core.user.resolver.CurrentUser;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}")
@RequiredArgsConstructor
public class WorkspaceUserController {

    private final WorkspaceUserService workspaceUserService;

    @GetMapping("/users")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER, WorkspaceUserRole.MEMBER})
    public ResponseEntity<WorkspaceUserListResponse> getWorkspaceUsers(
            @PathVariable Long workspaceId,
            @RequestParam(required = false) WorkspaceUserRole role
    ) {
        WorkspaceUserListResponse response = workspaceUserService.getWorkspaceUsers(workspaceId, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER, WorkspaceUserRole.MEMBER, WorkspaceUserRole.GUEST})
    public ResponseEntity<WorkspaceUserSimpleProfileResponse> getSimpleProfile(
            @PathVariable Long workspaceId,
            @CurrentUser Long userId
    ) {
        WorkspaceUserSimpleProfileResponse response = workspaceUserService.getSimpleProfile(workspaceId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{targetUserId}/profile")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER, WorkspaceUserRole.MEMBER, WorkspaceUserRole.GUEST})
    public ResponseEntity<WorkspaceUserFullProfileResponse> getFullProfile(
            @PathVariable Long workspaceId,
            @PathVariable Long targetUserId
    ) {
        WorkspaceUserFullProfileResponse response = workspaceUserService.getFullProfile(workspaceId, targetUserId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER, WorkspaceUserRole.MEMBER, WorkspaceUserRole.GUEST})
    public ResponseEntity<Void> updateProfile(
            @PathVariable Long workspaceId,
            @CurrentUser Long userId,
            @Valid @ModelAttribute WorkspaceUserProfileUpdateRequest request
    ) {
        workspaceUserService.updateProfile(workspaceId, userId, request);
        return ResponseEntity.noContent()
                             .build();
    }

    @PatchMapping("/notify")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER, WorkspaceUserRole.MEMBER, WorkspaceUserRole.GUEST})
    public ResponseEntity<Void> updateNotify(
            @PathVariable Long workspaceId,
            @CurrentUser Long userId,
            @Valid @RequestBody WorkspaceUserNotifyUpdateRequest request
    ) {
        workspaceUserService.updateNotify(workspaceId, userId, request);
        return ResponseEntity.noContent()
                             .build();
    }

    @PatchMapping("/state")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER, WorkspaceUserRole.MEMBER, WorkspaceUserRole.GUEST})
    public ResponseEntity<Void> updateState(
            @PathVariable Long workspaceId,
            @CurrentUser Long userId,
            @Valid @RequestBody WorkspaceUserStateUpdateRequest request
    ) {
        workspaceUserService.updateState(workspaceId, userId, request);
        return ResponseEntity.noContent()
                             .build();
    }

    @PatchMapping("/users/{targetUserId}/role")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Void> updateRole(
            @PathVariable Long workspaceId,
            @PathVariable Long targetUserId,
            @Valid @RequestBody WorkspaceUserRoleUpdateRequest request,
            @CurrentUser Long userId
    ) {
        workspaceUserService.updateRole(workspaceId, targetUserId, request.role(), userId);
        return ResponseEntity.noContent()
                             .build();
    }

    @DeleteMapping("/users/{targetUserId}")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Void> kickUser(
            @PathVariable Long workspaceId,
            @PathVariable Long targetUserId
    ) {
        workspaceUserService.kickUser(workspaceId, targetUserId);
        return ResponseEntity.noContent()
                             .build();
    }

    @PostMapping("/users/{targetUserId}/ban")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Void> banUser(
            @PathVariable Long workspaceId,
            @PathVariable Long targetUserId
    ) {
        workspaceUserService.banUser(workspaceId, targetUserId);
        return ResponseEntity.noContent()
                             .build();
    }

    @DeleteMapping("/users/{targetUserId}/ban")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Void> unbanUser(
            @PathVariable Long workspaceId,
            @PathVariable Long targetUserId
    ) {
        workspaceUserService.unbanUser(workspaceId, targetUserId);
        return ResponseEntity.noContent()
                             .build();
    }

    @DeleteMapping("/leave")
    @RequireWorkspaceRole({WorkspaceUserRole.MANAGER, WorkspaceUserRole.MEMBER, WorkspaceUserRole.GUEST})
    public ResponseEntity<Void> leaveWorkspace(
            @PathVariable Long workspaceId,
            @CurrentUser Long userId
    ) {
        workspaceUserService.leaveWorkspace(workspaceId, userId);
        return ResponseEntity.noContent()
                             .build();
    }
}