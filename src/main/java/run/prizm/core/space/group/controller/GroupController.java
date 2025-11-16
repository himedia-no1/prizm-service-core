package run.prizm.core.space.group.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.prizm.core.space.group.dto.GroupCreateRequest;
import run.prizm.core.space.group.entity.Group;
import run.prizm.core.space.group.service.GroupService;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<Group> createGroup(
            @PathVariable Long workspaceId,
            @Valid @RequestBody GroupCreateRequest request
    ) {
        Group group = groupService.createGroup(workspaceId, request);
        return ResponseEntity.ok(group);
    }
}