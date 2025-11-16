package run.prizm.core.space.group.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import run.prizm.core.security.permission.RequireWorkspaceRole;
import run.prizm.core.space.group.dto.*;
import run.prizm.core.space.group.entity.Group;
import run.prizm.core.space.group.service.GroupService;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Group> createGroup(
            @PathVariable Long workspaceId,
            @Valid @RequestBody GroupCreateRequest request
    ) {
        Group group = groupService.createGroup(workspaceId, request);
        return ResponseEntity.ok(group);
    }

    @GetMapping
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<GroupListResponse> getGroupList(
            @PathVariable Long workspaceId
    ) {
        GroupListResponse response = groupService.getGroupList(workspaceId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{groupId}")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<GroupDetailResponse> getGroupDetail(
            @PathVariable Long groupId
    ) {
        GroupDetailResponse response = groupService.getGroupDetail(groupId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{groupId}")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Group> updateGroup(
            @PathVariable Long groupId,
            @RequestBody GroupUpdateRequest request
    ) {
        Group group = groupService.updateGroup(groupId, request);
        return ResponseEntity.ok(group);
    }

    @DeleteMapping("/{groupId}")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Void> deleteGroup(
            @PathVariable Long groupId
    ) {
        groupService.deleteGroup(groupId);
        return ResponseEntity.noContent().build();
    }
}
