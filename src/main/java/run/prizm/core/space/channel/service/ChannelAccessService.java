package run.prizm.core.space.channel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.space.category.entity.Category;
import run.prizm.core.space.category.repository.CategoryRepository;
import run.prizm.core.space.channel.dto.AccessibleChannelListResponse;
import run.prizm.core.space.channel.dto.ChannelUserListResponse;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.permission.ChannelPermission;
import run.prizm.core.space.channel.permission.ChannelPermissionCalculator;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;
import run.prizm.core.storage.minio.MinioService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChannelAccessService {

    private final WorkspaceUserRepository workspaceUserRepository;
    private final CategoryRepository categoryRepository;
    private final ChannelPermissionCalculator permissionCalculator;
    private final MinioService minioService;

    @Cacheable(value = "channelAccess", key = "#workspaceId + ':' + #userId")
    @Transactional(readOnly = true)
    public AccessibleChannelListResponse getAccessibleChannels(Long workspaceId, Long userId) {
        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        Map<Long, ChannelPermission> permissions = permissionCalculator.calculatePermissions(workspaceUser);

        List<Category> categories = categoryRepository
                .findByWorkspaceIdAndDeletedAtIsNullOrderByZIndex(workspaceId);

        List<AccessibleChannelListResponse.CategoryWithChannels> result = new ArrayList<>();

        for (Category category : categories) {
            List<AccessibleChannelListResponse.ChannelItem> channels = category.getChannels()
                                                                               .stream()
                                                                               .filter(ch -> ch.getDeletedAt() == null)
                                                                               .filter(ch -> permissions.containsKey(ch.getId()))
                                                                               .filter(ch -> permissions.get(ch.getId()) != ChannelPermission.NONE)
                                                                               .sorted(Comparator.comparing(Channel::getZIndex))
                                                                               .map(ch -> new AccessibleChannelListResponse.ChannelItem(
                                                                                       ch.getId(),
                                                                                       ch.getName(),
                                                                                       permissions.get(ch.getId())
                                                                                                  .name()
                                                                               ))
                                                                               .toList();

            if (!channels.isEmpty()) {
                result.add(new AccessibleChannelListResponse.CategoryWithChannels(
                        category.getId(),
                        category.getName(),
                        channels
                ));
            }
        }

        return new AccessibleChannelListResponse(result);
    }

    @Transactional(readOnly = true)
    public ChannelUserListResponse getChannelUsers(Long workspaceId, Long channelId) {
        List<WorkspaceUser> allWorkspaceUsers = workspaceUserRepository
                .findByWorkspaceIdAndDeletedAtIsNull(workspaceId);

        List<ChannelUserListResponse.UserItem> regularUsers = new ArrayList<>();
        List<ChannelUserListResponse.UserItem> guestUsers = new ArrayList<>();

        for (WorkspaceUser wu : allWorkspaceUsers) {
            Map<Long, ChannelPermission> permissions = permissionCalculator.calculatePermissions(wu);

            if (permissions.containsKey(channelId) && permissions.get(channelId) != ChannelPermission.NONE) {
                String image = wu.getImage() != null
                        ? minioService.getFileUrl(wu.getImage()
                                                    .getPath())
                        : (wu.getUser()
                             .getImage() != null
                        ? minioService.getFileUrl(wu.getUser()
                                                    .getImage()
                                                    .getPath())
                        : null);
                String name = wu.getName() != null ? wu.getName() : wu.getUser()
                                                                      .getName();

                ChannelUserListResponse.UserItem item = new ChannelUserListResponse.UserItem(
                        wu.getId(),
                        wu.getState(),
                        image,
                        name
                );

                if (wu.getRole() == WorkspaceUserRole.GUEST) {
                    guestUsers.add(item);
                } else {
                    regularUsers.add(item);
                }
            }
        }

        regularUsers.sort(Comparator.comparing(ChannelUserListResponse.UserItem::name));
        guestUsers.sort(Comparator.comparing(ChannelUserListResponse.UserItem::name));

        return new ChannelUserListResponse(regularUsers, guestUsers);
    }

    @CacheEvict(value = "channelAccess", key = "#workspaceId + ':' + #userId")
    public void invalidateCache(Long workspaceId, Long userId) {
    }

    @CacheEvict(value = "channelAccess", allEntries = true)
    public void invalidateWorkspaceCache(Long workspaceId) {
    }

    @Transactional(readOnly = true)
    public String getChannelPermission(Long workspaceId, Long userId, Long channelId) {
        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        Map<Long, ChannelPermission> permissions = permissionCalculator.calculatePermissions(workspaceUser);

        ChannelPermission permission = permissions.getOrDefault(channelId, ChannelPermission.NONE);

        return permission.name();
    }
}