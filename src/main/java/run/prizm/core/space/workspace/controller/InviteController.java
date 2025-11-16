package run.prizm.core.space.workspace.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.resolver.CurrentUser;
import run.prizm.core.space.workspace.dto.WorkspaceInviteInfoResponse;
import run.prizm.core.space.workspace.dto.WorkspaceInviteJoinResponse;
import run.prizm.core.space.workspace.service.WorkspaceInviteService;

@RestController
@RequestMapping("/api/invite")
@RequiredArgsConstructor
public class InviteController {

    private final WorkspaceInviteService workspaceInviteService;

    @GetMapping("/{inviteCode}")
    public ResponseEntity<WorkspaceInviteInfoResponse> getInviteInfo(@PathVariable @NotBlank String inviteCode) {
        WorkspaceInviteInfoResponse response = workspaceInviteService.getInviteInfo(inviteCode);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{inviteCode}/join")
    public ResponseEntity<WorkspaceInviteJoinResponse> joinWorkspace(
            @PathVariable @NotBlank String inviteCode,
            @CurrentUser User user
    ) {
        WorkspaceInviteJoinResponse response = workspaceInviteService.joinByInvite(inviteCode, user.getId());
        return ResponseEntity.ok(response);
    }
}