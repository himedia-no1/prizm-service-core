package run.prizm.core.space.channel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import run.prizm.core.security.permission.RequireWorkspaceRole;
import run.prizm.core.space.channel.dto.*;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.service.ChannelAccessService;
import run.prizm.core.space.channel.service.ChannelService;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.user.resolver.CurrentUser;

@RestController
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;
    private final ChannelAccessService channelAccessService;

    @PostMapping("/api/workspaces/{workspaceId}/categories/{categoryId}/channels")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Channel> createChannel(
            @PathVariable Long workspaceId,
            @PathVariable Long categoryId,
            @Valid @RequestBody ChannelCreateRequest request
    ) {
        Channel channel = channelService.createChannel(workspaceId, categoryId, request);
        channelAccessService.invalidateWorkspaceCache(workspaceId);
        return ResponseEntity.ok(channel);
    }

    @GetMapping("/api/workspaces/{workspaceId}/channels/{channelId}")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER, WorkspaceUserRole.MEMBER, WorkspaceUserRole.GUEST})
    public ResponseEntity<ChannelInfoResponse> getChannelInfo(
            @PathVariable Long channelId,
            @CurrentUser Long userId
    ) {
        ChannelInfoResponse response = channelService.getChannelInfo(channelId, userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/api/workspaces/{workspaceId}/channels/{channelId}")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Channel> updateChannel(
            @PathVariable Long workspaceId,
            @PathVariable Long channelId,
            @RequestBody ChannelUpdateRequest request
    ) {
        Channel channel = channelService.updateChannel(channelId, request);
        channelAccessService.invalidateWorkspaceCache(workspaceId);
        return ResponseEntity.ok(channel);
    }

    @PatchMapping("/api/workspaces/{workspaceId}/channels/{channelId}/z-index")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Void> updateZIndex(
            @PathVariable Long channelId,
            @RequestBody ChannelZIndexUpdateRequest request
    ) {
        channelService.updateZIndex(channelId, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/workspaces/{workspaceId}/channels/{channelId}/notify")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER, WorkspaceUserRole.MEMBER, WorkspaceUserRole.GUEST})
    public ResponseEntity<Void> updateNotify(
            @PathVariable Long channelId,
            @CurrentUser Long workspaceUserId,
            @RequestBody ChannelNotifyUpdateRequest request
    ) {
        channelService.updateNotify(channelId, workspaceUserId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/workspaces/{workspaceId}/channels/{channelId}")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER})
    public ResponseEntity<Void> deleteChannel(
            @PathVariable Long workspaceId,
            @PathVariable Long channelId
    ) {
        channelService.deleteChannel(channelId);
        channelAccessService.invalidateWorkspaceCache(workspaceId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/workspaces/{workspaceId}/channels/accessible")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER, WorkspaceUserRole.MEMBER, WorkspaceUserRole.GUEST})
    public ResponseEntity<AccessibleChannelListResponse> getAccessibleChannels(
            @PathVariable Long workspaceId,
            @CurrentUser Long userId
    ) {
        AccessibleChannelListResponse response = channelAccessService.getAccessibleChannels(workspaceId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/workspaces/{workspaceId}/channels/{channelId}/users")
    @RequireWorkspaceRole({WorkspaceUserRole.OWNER, WorkspaceUserRole.MANAGER, WorkspaceUserRole.MEMBER, WorkspaceUserRole.GUEST})
    public ResponseEntity<ChannelUserListResponse> getChannelUsers(
            @PathVariable Long workspaceId,
            @PathVariable Long channelId
    ) {
        ChannelUserListResponse response = channelAccessService.getChannelUsers(workspaceId, channelId);
        return ResponseEntity.ok(response);
    }
}
