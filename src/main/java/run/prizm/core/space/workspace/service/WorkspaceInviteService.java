package run.prizm.core.space.workspace.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.space.channel.constraint.ChannelWorkspaceUserNotify;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.entity.ChannelWorkspaceUser;
import run.prizm.core.space.channel.repository.ChannelRepository;
import run.prizm.core.space.channel.repository.ChannelWorkspaceUserRepository;
import run.prizm.core.space.channel.service.ChannelAccessService;
import run.prizm.core.space.group.entity.Group;
import run.prizm.core.space.group.entity.GroupWorkspaceUser;
import run.prizm.core.space.group.repository.GroupRepository;
import run.prizm.core.space.group.repository.GroupWorkspaceUserRepository;
import run.prizm.core.space.workspace.cache.WorkspaceInviteCache;
import run.prizm.core.space.workspace.constraint.WorkspaceUserNotify;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.constraint.WorkspaceUserState;
import run.prizm.core.space.workspace.dto.*;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.repository.WorkspaceInviteCacheRepository;
import run.prizm.core.space.workspace.repository.WorkspaceRepository;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;
import run.prizm.core.storage.minio.MinioService;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.repository.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkspaceInviteService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final UserRepository userRepository;
    private final WorkspaceInviteCacheRepository inviteCacheRepository;
    private final ChannelRepository channelRepository;
    private final ChannelWorkspaceUserRepository channelWorkspaceUserRepository;
    private final GroupRepository groupRepository;
    private final GroupWorkspaceUserRepository groupWorkspaceUserRepository;
    private final MinioService minioService;
    private final ChannelAccessService channelAccessService;

    @Transactional
    public WorkspaceInviteCreateResponse createMemberInvite(
            Long workspaceId,
            Long creatorUserId,
            WorkspaceInviteCreateRequest request
    ) {
        validateCreator(workspaceId, creatorUserId);

        if (request.allowedUserIds() != null && !request.allowedUserIds()
                                                        .isEmpty()) {
            for (Long userId : request.allowedUserIds()) {
                if (!userRepository.existsById(userId)) {
                    throw new BusinessException(ErrorCode.ALLOWED_USER_NOT_FOUND);
                }
            }
        }

        Instant now = Instant.now();
        Instant expiresAt = request.expiresInSeconds() != null
                ? now.plusSeconds(request.expiresInSeconds())
                : null;

        String code = UUID.randomUUID()
                          .toString()
                          .replace("-", "");

        WorkspaceInviteCache cache = new WorkspaceInviteCache(
                code,
                workspaceId,
                null,
                expiresAt != null ? expiresAt.toEpochMilli() : null,
                request.maxUses(),
                0,
                now.toEpochMilli()
        );
        cache.setRole(WorkspaceUserRole.MEMBER);
        cache.setAllowedUserIds(request.allowedUserIds());
        cache.setAutoJoinGroupIds(request.autoJoinGroupIds());

        inviteCacheRepository.save(cache);

        return new WorkspaceInviteCreateResponse(code, expiresAt, request.maxUses(), null);
    }

    @Transactional
    public WorkspaceInviteCreateResponse createGuestInvite(
            Long workspaceId,
            Long creatorUserId,
            Long channelId,
            List<Long> allowedUserIds
    ) {
        WorkspaceUser creator = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, creatorUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        Channel channel = channelRepository.findById(channelId)
                                           .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        if (!channel.getWorkspace()
                    .getId()
                    .equals(workspaceId)) {
            throw new BusinessException(ErrorCode.CHANNEL_NOT_IN_WORKSPACE);
        }

        if (creator.getRole() == WorkspaceUserRole.MEMBER) {
            String permission = channelAccessService.getChannelPermission(workspaceId, creatorUserId, channelId);
            if (!"MANAGE".equals(permission)) {
                throw new BusinessException(ErrorCode.MEMBER_REQUIRES_MANAGE_PERMISSION);
            }
        } else if (creator.getRole() != WorkspaceUserRole.OWNER && creator.getRole() != WorkspaceUserRole.MANAGER) {
            throw new BusinessException(ErrorCode.GUEST_INVITE_PERMISSION_DENIED);
        }

        if (allowedUserIds == null || allowedUserIds.isEmpty()) {
            throw new BusinessException(ErrorCode.GUEST_INVITE_REQUIRES_ALLOWED_USERS);
        }

        for (Long userId : allowedUserIds) {
            if (!userRepository.existsById(userId)) {
                throw new BusinessException(ErrorCode.ALLOWED_USER_NOT_FOUND);
            }
        }

        String code = UUID.randomUUID()
                          .toString()
                          .replace("-", "");
        Instant now = Instant.now();

        WorkspaceInviteCache cache = new WorkspaceInviteCache(
                code,
                workspaceId,
                null,
                null,
                null,
                0,
                now.toEpochMilli()
        );
        cache.setRole(WorkspaceUserRole.GUEST);
        cache.setChannelId(channelId);
        cache.setAllowedUserIds(allowedUserIds);

        inviteCacheRepository.save(cache);

        return new WorkspaceInviteCreateResponse(code, null, null, channelId);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceInviteInfoResponse> getInviteList(Long workspaceId) {
        List<WorkspaceInviteCache> caches = inviteCacheRepository.findByWorkspaceId(workspaceId);

        return caches.stream()
                     .filter(cache -> !cache.isExpired() && !cache.hasReachedMaxUses())
                     .map(cache -> {
                         Instant expiresAt = cache.getExpiresAt() != null
                                 ? Instant.ofEpochMilli(cache.getExpiresAt())
                                 : null;
                         Instant createdAt = Instant.ofEpochMilli(cache.getCreatedAt());

                         String location = cache.getRole() == WorkspaceUserRole.GUEST && cache.getChannelId() != null
                                 ? channelRepository.findById(cache.getChannelId())
                                                    .map(Channel::getName)
                                                    .orElse("Unknown Channel")
                                 : "Workspace";

                         return new WorkspaceInviteInfoResponse(
                                 cache.getCode(),
                                 createdAt,
                                 expiresAt,
                                 cache.getUsageCount(),
                                 cache.getMaxUses(),
                                 location
                         );
                     })
                     .collect(Collectors.toList());
    }

    @Transactional
    public void deleteInvite(Long workspaceId, String code) {
        WorkspaceInviteCache cache = inviteCacheRepository.find(code)
                                                          .orElseThrow(() -> new BusinessException(ErrorCode.INVITE_NOT_FOUND));

        if (!cache.getWorkspaceId()
                  .equals(workspaceId)) {
            throw new BusinessException(ErrorCode.INVITE_NOT_FOR_WORKSPACE);
        }

        inviteCacheRepository.delete(code);
    }

    @Transactional
    public WorkspaceInviteJoinResponse joinByInvite(String inviteCode, Long userId) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        WorkspaceInviteCache cache = inviteCacheRepository.find(inviteCode)
                                                          .orElseThrow(() -> new BusinessException(ErrorCode.INVITE_NOT_FOUND));

        if (cache.isExpired()) {
            inviteCacheRepository.delete(inviteCode);
            throw new BusinessException(ErrorCode.INVITE_EXPIRED);
        }

        if (cache.hasReachedMaxUses()) {
            inviteCacheRepository.delete(inviteCode);
            throw new BusinessException(ErrorCode.INVITE_USAGE_LIMIT_REACHED);
        }

        if (cache.getAllowedUserIds() != null && !cache.getAllowedUserIds()
                                                       .isEmpty()) {
            if (!cache.getAllowedUserIds()
                      .contains(userId)) {
                throw new BusinessException(ErrorCode.USER_NOT_ALLOWED_FOR_INVITE);
            }
        }

        Workspace workspace = workspaceRepository.findById(cache.getWorkspaceId())
                                                 .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_NOT_FOUND));

        WorkspaceUser existingUser = workspaceUserRepository
                .findByWorkspaceIdAndUserId(workspace.getId(), userId)
                .orElse(null);

        if (existingUser != null && existingUser.isBanned()) {
            throw new BusinessException(ErrorCode.USER_BANNED_FROM_WORKSPACE);
        }

        if (existingUser != null && existingUser.getDeletedAt() == null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_IN_WORKSPACE);
        }

        WorkspaceUserRole role = cache.getRole() != null ? cache.getRole() : WorkspaceUserRole.MEMBER;

        WorkspaceUser workspaceUser = WorkspaceUser.builder()
                                                   .workspace(workspace)
                                                   .user(user)
                                                   .role(role)
                                                   .image(null)
                                                   .name(null)

                                                   .state(WorkspaceUserState.ONLINE)
                                                   .notify(WorkspaceUserNotify.ON)
                                                   .banned(false)
                                                   .build();
        workspaceUserRepository.save(workspaceUser);

        if (role == WorkspaceUserRole.MEMBER && cache.getAutoJoinGroupIds() != null) {
            for (Long groupId : cache.getAutoJoinGroupIds()) {
                Group group = groupRepository.findById(groupId)
                                             .orElse(null);
                if (group != null && group.getDeletedAt() == null) {
                    GroupWorkspaceUser gwu = GroupWorkspaceUser.builder()
                                                               .group(group)
                                                               .workspaceUser(workspaceUser)
                                                               .build();
                    groupWorkspaceUserRepository.save(gwu);
                }
            }
        }

        if (role == WorkspaceUserRole.GUEST && cache.getChannelId() != null) {
            Channel channel = channelRepository.findById(cache.getChannelId())
                                               .orElse(null);
            if (channel != null && channel.getDeletedAt() == null) {
                ChannelWorkspaceUser cwu = ChannelWorkspaceUser.builder()
                                                               .channel(channel)
                                                               .workspaceUser(workspaceUser)
                                                               .explicit(true)
                                                               .notify(ChannelWorkspaceUserNotify.ON)
                                                               .build();
                channelWorkspaceUserRepository.save(cwu);
            }
        }

        cache.incrementUsage();
        if (cache.hasReachedMaxUses()) {
            inviteCacheRepository.delete(inviteCode);
        } else {
            inviteCacheRepository.save(cache);
        }

        return new WorkspaceInviteJoinResponse(workspace.getId(), workspaceUser.getId(), role.name());
    }

    private void validateCreator(Long workspaceId, Long userId) {
        boolean isMember = workspaceUserRepository.existsByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId);
        if (!isMember) {
            throw new BusinessException(ErrorCode.INVALID_INVITE_PERMISSION);
        }
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspaceByInviteCode(String inviteCode) {
        WorkspaceInviteCache cache = inviteCacheRepository.find(inviteCode)
                                                          .orElseThrow(() -> new BusinessException(ErrorCode.INVITE_NOT_FOUND));

        if (cache.isExpired()) {
            throw new BusinessException(ErrorCode.INVITE_EXPIRED);
        }

        Workspace workspace = workspaceRepository.findById(cache.getWorkspaceId())
                                                 .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_NOT_FOUND));

        if (workspace.getDeletedAt() != null) {
            throw new BusinessException(ErrorCode.WORKSPACE_DELETED);
        }

        String imageUrl = workspace.getImage() != null
                ? minioService.getFileUrl(workspace.getImage()
                                                   .getPath())
                : null;

        return new WorkspaceResponse(
                workspace.getId(),
                workspace.getName(),
                imageUrl,
                workspace.getCreatedAt()
        );
    }
}