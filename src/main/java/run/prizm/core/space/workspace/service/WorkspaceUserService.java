package run.prizm.core.space.workspace.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.common.constant.FileDirectory;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.common.util.ImageUploadHelper;
import run.prizm.core.file.entity.File;
import run.prizm.core.space.channel.service.ChannelAccessService;
import run.prizm.core.space.group.repository.GroupWorkspaceUserRepository;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.dto.*;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;
import run.prizm.core.user.repository.UserRepository;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceUserService {

    private final WorkspaceUserRepository workspaceUserRepository;
    private final UserRepository userRepository;
    private final GroupWorkspaceUserRepository groupWorkspaceUserRepository;
    private final ImageUploadHelper imageUploadHelper;
    private final ChannelAccessService channelAccessService;

    @Transactional(readOnly = true)
    public WorkspaceUserListResponse getWorkspaceUsers(Long workspaceId, WorkspaceUserRole roleFilter) {
        List<WorkspaceUser> workspaceUsers;

        if (roleFilter != null) {
            workspaceUsers = workspaceUserRepository.findByWorkspaceIdAndRoleAndDeletedAtIsNull(workspaceId, roleFilter);
        } else {
            workspaceUsers = workspaceUserRepository.findByWorkspaceIdAndDeletedAtIsNull(workspaceId);
        }

        List<WorkspaceUserListResponse.WorkspaceUserItem> items = workspaceUsers.stream()
                                                                                .map(wu -> {
                                                                                    String image = wu.getImage() != null
                                                                                            ? imageUploadHelper.getImageUrl(wu.getImage())
                                                                                            : (wu.getUser()
                                                                                                 .getImage() != null
                                                                                            ? imageUploadHelper.getImageUrl(wu.getUser()
                                                                                                                              .getImage())
                                                                                            : null);
                                                                                    String name = wu.getName() != null ? wu.getName() : wu.getUser()
                                                                                                                                          .getName();
                                                                                    String email = wu.getUser()
                                                                                                     .getEmail();
                                                                                    return new WorkspaceUserListResponse.WorkspaceUserItem(
                                                                                            wu.getId(), wu.getState(), image, name, email
                                                                                    );
                                                                                })
                                                                                .toList();

        return new WorkspaceUserListResponse(items);
    }

    @Transactional(readOnly = true)
    public WorkspaceUserSimpleProfileResponse getSimpleProfile(Long workspaceId, Long userId) {
        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        String image = workspaceUser.getImage() != null
                ? imageUploadHelper.getImageUrl(workspaceUser.getImage())
                : imageUploadHelper.getImageUrl(workspaceUser.getUser()
                                                             .getImage());
        String name = workspaceUser.getName() != null
                ? workspaceUser.getName()
                : workspaceUser.getUser()
                               .getName();

        return new WorkspaceUserSimpleProfileResponse(
                workspaceUser.getId(),  // workspaceUserId 추가
                workspaceUser.getRole(),
                workspaceUser.getNotify(),
                workspaceUser.getState(),
                image,
                name
        );
    }

    @Transactional
    public void updateProfile(Long workspaceId, Long userId, WorkspaceUserProfileUpdateRequest request) {
        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        if (request.image() != null && !request.image()
                                               .isEmpty()) {
            if (workspaceUser.getImage() != null) {
                imageUploadHelper.deleteImage(workspaceUser.getImage());
            }

            File newImage = imageUploadHelper.uploadImage(request.image(), FileDirectory.WORKSPACE_PROFILES.getPath());
            workspaceUser.setImage(newImage);
        }

        if (request.name() != null) {
            workspaceUser.setName(request.name());
        }

        if (request.phone() != null) {
            workspaceUser.setPhone(request.phone());
        }

        if (request.introduction() != null) {
            workspaceUser.setIntroduction(request.introduction());
        }

        workspaceUserRepository.save(workspaceUser);
    }

    @Transactional
    public void updateNotify(Long workspaceId, Long userId, WorkspaceUserNotifyUpdateRequest request) {
        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        workspaceUser.setNotify(request.notifyType());
        workspaceUserRepository.save(workspaceUser);
    }

    @Transactional
    public void updateState(Long workspaceId, Long userId, WorkspaceUserStateUpdateRequest request) {
        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        workspaceUser.setState(request.state());
        workspaceUserRepository.save(workspaceUser);
    }

    @Transactional
    public void updateRole(Long workspaceId, Long targetUserId, WorkspaceUserRole newRole, Long requesterId) {
        WorkspaceUser requester = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, requesterId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        WorkspaceUser target = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        if (requester.getRole() != WorkspaceUserRole.OWNER && requester.getRole() != WorkspaceUserRole.MANAGER) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_PERMISSION);
        }

        if (newRole == WorkspaceUserRole.OWNER) {
            if (requester.getRole() != WorkspaceUserRole.OWNER) {
                throw new BusinessException(ErrorCode.OWNER_DELEGATION_FORBIDDEN);
            }
            requester.setRole(WorkspaceUserRole.MANAGER);
            workspaceUserRepository.save(requester);
        }

        target.setRole(newRole);
        workspaceUserRepository.save(target);

        channelAccessService.invalidateCache(workspaceId, targetUserId);
        channelAccessService.invalidateCache(workspaceId, requesterId);
    }

    @Transactional
    public void kickUser(Long workspaceId, Long targetUserId) {
        WorkspaceUser target = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        target.setDeletedAt(Instant.now());
        workspaceUserRepository.save(target);
    }

    @Transactional
    public void banUser(Long workspaceId, Long targetUserId) {
        WorkspaceUser target = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        target.setBanned(true);
        target.setDeletedAt(Instant.now());
        workspaceUserRepository.save(target);
    }

    @Transactional
    public void unbanUser(Long workspaceId, Long targetUserId) {
        WorkspaceUser target = workspaceUserRepository
                .findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        target.setBanned(false);
        workspaceUserRepository.save(target);
    }

    @Transactional
    public void leaveWorkspace(Long workspaceId, Long userId) {
        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        if (workspaceUser.getRole() == WorkspaceUserRole.OWNER) {
            throw new BusinessException(ErrorCode.OWNER_CANNOT_LEAVE);
        }

        workspaceUser.setDeletedAt(Instant.now());
        workspaceUserRepository.save(workspaceUser);
    }

    @Transactional(readOnly = true)
    public WorkspaceUserFullProfileResponse getFullProfile(Long workspaceId, Long targetUserId) {
        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByWorkspaceIdAndUserId(workspaceId, targetUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        String image = workspaceUser.getImage() != null
                ? imageUploadHelper.getImageUrl(workspaceUser.getImage())
                : (workspaceUser.getUser()
                                .getImage() != null
                ? imageUploadHelper.getImageUrl(workspaceUser.getUser()
                                                             .getImage())
                : null);

        String email = workspaceUser.getUser()
                                    .getEmail();

        List<WorkspaceUserFullProfileResponse.GroupInfo> groups = groupWorkspaceUserRepository
                .findByWorkspaceUserAndGroupDeletedAtIsNull(workspaceUser)
                .stream()
                .map(gwu -> new WorkspaceUserFullProfileResponse.GroupInfo(
                        gwu.getGroup()
                           .getId(),
                        gwu.getGroup()
                           .getName()
                ))
                .sorted((a, b) -> a.name()
                                   .compareTo(b.name()))
                .toList();

        return new WorkspaceUserFullProfileResponse(
                workspaceUser.getRole(),
                workspaceUser.getState(),
                image,
                workspaceUser.getUser()
                             .getName(),
                workspaceUser.getName(),
                email,
                workspaceUser.getUser()
                             .getAuthProvider(),
                workspaceUser.getPhone(),
                workspaceUser.getIntroduction(),
                workspaceUser.getUser()
                             .getCreatedAt(),
                workspaceUser.getCreatedAt(),
                groups
        );
    }
}