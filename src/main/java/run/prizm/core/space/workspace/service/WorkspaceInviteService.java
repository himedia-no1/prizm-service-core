package run.prizm.core.space.workspace.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.repository.UserRepository;
import run.prizm.core.common.id.UuidV7LongGenerator;
import run.prizm.core.space.workspace.cache.WorkspaceInviteCache;
import run.prizm.core.space.workspace.dto.WorkspaceInviteCreateRequest;
import run.prizm.core.space.workspace.dto.WorkspaceInviteCreateResponse;
import run.prizm.core.space.workspace.dto.WorkspaceInviteInfoResponse;
import run.prizm.core.space.workspace.dto.WorkspaceInviteJoinResponse;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.constraint.WorkspaceUserNotify;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.constraint.WorkspaceUserState;
import run.prizm.core.space.workspace.repository.WorkspaceRepository;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;
import run.prizm.core.space.workspace.repository.WorkspaceInviteCacheRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class WorkspaceInviteService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final UserRepository userRepository;
    private final WorkspaceInviteCacheRepository inviteCacheRepository;

    @Transactional
    public WorkspaceInviteCreateResponse createInvite(Long workspaceId, Long creatorUserId, WorkspaceInviteCreateRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                                                 .orElseThrow(() -> new RuntimeException("Workspace not found"));

        boolean isMember = workspaceUserRepository.existsByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, creatorUserId);
        if (!isMember) {
            throw new RuntimeException("User not allowed to create invite");
        }

        if (request.allowedUserId() != null) {
            userRepository.findById(request.allowedUserId())
                          .orElseThrow(() -> new RuntimeException("Allowed user not found"));
        }

        Instant now = Instant.now();
        Instant expiresAt = request.expiresInSeconds() != null ? now.plusSeconds(request.expiresInSeconds()) : null;

        WorkspaceInviteCache cache = new WorkspaceInviteCache(
                String.valueOf(UuidV7LongGenerator.nextValue()),
                workspaceId,
                request.allowedUserId(),
                expiresAt != null ? expiresAt.toEpochMilli() : null,
                request.maxUses(),
                0,
                now.toEpochMilli()
        );

        inviteCacheRepository.save(cache);

        return new WorkspaceInviteCreateResponse(cache.getCode(), expiresAt, request.maxUses(), request.allowedUserId());
    }

    public WorkspaceInviteInfoResponse getInviteInfo(String inviteCode) {
        WorkspaceInviteCache cache = inviteCacheRepository.find(inviteCode)
                                                          .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (cache.isExpired() || cache.hasReachedMaxUses()) {
            inviteCacheRepository.delete(inviteCode);
            throw new RuntimeException("Invite unavailable");
        }

        Workspace workspace = workspaceRepository.findById(cache.getWorkspaceId())
                                                 .orElseThrow(() -> new RuntimeException("Workspace not found"));
        long memberCount = workspaceUserRepository.countByWorkspaceIdAndDeletedAtIsNull(workspace.getId());
        Long imageId = workspace.getImage() != null ? workspace.getImage()
                                                           .getId() : null;
        return new WorkspaceInviteInfoResponse(workspace.getId(), workspace.getName(), imageId, memberCount);
    }

    @Transactional
    public WorkspaceInviteJoinResponse joinByInvite(String inviteCode, Long userId) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new RuntimeException("User not found"));

        WorkspaceInviteCache cache = inviteCacheRepository.find(inviteCode)
                                                          .orElseThrow(() -> new RuntimeException("Invite not found"));

        if (cache.isExpired()) {
            inviteCacheRepository.delete(inviteCode);
            throw new RuntimeException("Invite expired");
        }

        if (cache.hasReachedMaxUses()) {
            inviteCacheRepository.delete(inviteCode);
            throw new RuntimeException("Invite usage limit reached");
        }

        if (cache.getAllowedUserId() != null && !cache.getAllowedUserId()
                                                      .equals(userId)) {
            throw new RuntimeException("Invite restricted to another user");
        }

        boolean alreadyMember = workspaceUserRepository.existsByWorkspaceIdAndUserIdAndDeletedAtIsNull(cache.getWorkspaceId(), userId);
        if (alreadyMember) {
            throw new RuntimeException("User already joined workspace");
        }

        Workspace workspace = workspaceRepository.findById(cache.getWorkspaceId())
                                                 .orElseThrow(() -> new RuntimeException("Workspace not found"));

        workspaceUserRepository.save(
                WorkspaceUser.builder()
                             .workspace(workspace)
                             .user(user)
                             .role(WorkspaceUserRole.MEMBER)
                             .image(user.getImage())
                             .name(user.getName())
                             .email(user.getEmail())
                             .state(WorkspaceUserState.ONLINE)
                             .notify(WorkspaceUserNotify.ON)
                             .banned(false)
                             .build()
        );

        cache.incrementUsage();
        if (cache.hasReachedMaxUses()) {
            inviteCacheRepository.delete(inviteCode);
        } else {
            inviteCacheRepository.save(cache);
        }

        return new WorkspaceInviteJoinResponse(workspace.getId(), userId, WorkspaceUserRole.MEMBER.name());
    }
}