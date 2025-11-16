package run.prizm.core.space.channel.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.prizm.core.space.channel.dto.ChannelCreateRequest;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.service.ChannelService;

@RestController
@RequestMapping("/api/workspaces/{workspaceId}/categories/{categoryId}/channels")
@RequiredArgsConstructor
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    public ResponseEntity<Channel> createChannel(
            @PathVariable Long workspaceId,
            @PathVariable Long categoryId,
            @Valid @RequestBody ChannelCreateRequest request
    ) {
        Channel channel = channelService.createChannel(workspaceId, categoryId, request);
        return ResponseEntity.ok(channel);
    }
}