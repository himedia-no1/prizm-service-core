package run.prizm.core.space.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.space.category.repository.CategoryRepository;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.repository.ChannelRepository;
import run.prizm.core.space.channel.service.ChannelAccessService;
import run.prizm.core.space.group.dto.*;
import run.prizm.core.space.group.entity.Group;
import run.prizm.core.space.group.entity.GroupChannel;
import run.prizm.core.space.group.entity.GroupWorkspaceUser;
import run.prizm.core.space.group.repository.GroupChannelRepository;
import run.prizm.core.space.group.repository.GroupRepository;
import run.prizm.core.space.group.repository.GroupWorkspaceUserRepository;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.repository.WorkspaceRepository;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final WorkspaceRepository workspaceRepository;
    private final GroupWorkspaceUserRepository groupWorkspaceUserRepository;
    private final GroupChannelRepository groupChannelRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final ChannelRepository channelRepository;
    private final CategoryRepository categoryRepository;
    private final ChannelAccessService channelAccessService;

    @Transactional
    public GroupResponse createGroup(Long workspaceId, GroupCreateRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                                                 .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_NOT_FOUND));

        Group group = Group.builder()
                           .workspace(workspace)
                           .name(request.name())
                           .build();

        group = groupRepository.save(group);

        return toResponse(group);
    }

    @Transactional(readOnly = true)
    public GroupListResponse getGroupList(Long workspaceId) {
        List<Group> groups = groupRepository.findByWorkspaceIdAndDeletedAtIsNullOrderByName(workspaceId);

        List<GroupListResponse.GroupItem> items = groups.stream()
                                                        .map(group -> new GroupListResponse.GroupItem(group.getId(), group.getName()))
                                                        .toList();

        return new GroupListResponse(items);
    }

    @Transactional(readOnly = true)
    public GroupDetailResponse getGroupDetail(Long groupId) {
        Group group = groupRepository.findById(groupId)
                                     .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        List<GroupWorkspaceUser> groupWorkspaceUsers = groupWorkspaceUserRepository
                .findByGroupIdAndWorkspaceUserDeletedAtIsNull(groupId);

        List<GroupDetailResponse.UserItem> users = groupWorkspaceUsers.stream()
                                                                      .map(gwu -> {
                                                                          String name = gwu.getWorkspaceUser()
                                                                                           .getName() != null
                                                                                  ? gwu.getWorkspaceUser()
                                                                                       .getName()
                                                                                  : gwu.getWorkspaceUser()
                                                                                       .getUser()
                                                                                       .getName();
                                                                          return new GroupDetailResponse.UserItem(gwu.getWorkspaceUser()
                                                                                                                     .getId(), name);
                                                                      })
                                                                      .sorted((a, b) -> a.name()
                                                                                         .compareTo(b.name()))
                                                                      .toList();

        List<GroupChannel> groupChannels = groupChannelRepository.findByGroupIdAndChannelDeletedAtIsNull(groupId);

        Map<Long, List<GroupChannel>> channelsByCategory = groupChannels.stream()
                                                                        .collect(Collectors.groupingBy(gc -> gc.getChannel()
                                                                                                               .getCategory()
                                                                                                               .getId()));

        List<GroupDetailResponse.CategoryWithChannels> categories = channelsByCategory.entrySet()
                                                                                      .stream()
                                                                                      .map(entry -> {
                                                                                          Long categoryId = entry.getKey();
                                                                                          var category = categoryRepository.findById(categoryId)
                                                                                                                           .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

                                                                                          List<GroupDetailResponse.ChannelItem> channels = entry.getValue()
                                                                                                                                                .stream()
                                                                                                                                                .sorted((a, b) -> a.getChannel()
                                                                                                                                                                   .getZIndex()
                                                                                                                                                                   .compareTo(b.getChannel()
                                                                                                                                                                               .getZIndex()))
                                                                                                                                                .map(gc -> new GroupDetailResponse.ChannelItem(
                                                                                                                                                        gc.getChannel()
                                                                                                                                                          .getId(),
                                                                                                                                                        gc.getChannel()
                                                                                                                                                          .getName(),
                                                                                                                                                        gc.getPermission()
                                                                                                                                                ))
                                                                                                                                                .toList();

                                                                                          return new GroupDetailResponse.CategoryWithChannels(
                                                                                                  category.getId(),
                                                                                                  category.getName(),
                                                                                                  channels
                                                                                          );
                                                                                      })
                                                                                      .sorted((a, b) -> a.name()
                                                                                                         .compareTo(b.name()))
                                                                                      .toList();

        return new GroupDetailResponse(group.getId(), group.getName(), users, categories);
    }

    @Transactional
    public GroupResponse updateGroup(Long groupId, GroupUpdateRequest request) {
        Group group = groupRepository.findById(groupId)
                                     .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        if (request.name() != null) {
            group.setName(request.name());
        }

        groupWorkspaceUserRepository.deleteByGroupId(groupId);

        if (request.userIds() != null && !request.userIds()
                                                 .isEmpty()) {
            for (Long workspaceUserId : request.userIds()) {
                WorkspaceUser workspaceUser = workspaceUserRepository.findById(workspaceUserId)
                                                                     .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

                if (workspaceUser.getRole() == WorkspaceUserRole.GUEST) {
                    throw new BusinessException(ErrorCode.CANNOT_ASSIGN_GUEST_TO_GROUP);
                }

                GroupWorkspaceUser gwu = GroupWorkspaceUser.builder()
                                                           .group(group)
                                                           .workspaceUser(workspaceUser)
                                                           .build();
                groupWorkspaceUserRepository.save(gwu);
            }
        }

        groupChannelRepository.deleteByGroupId(groupId);

        if (request.channels() != null && !request.channels()
                                                  .isEmpty()) {
            for (var channelItem : request.channels()) {
                Channel channel = channelRepository.findById(channelItem.channelId())
                                                   .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

                GroupChannel gc = GroupChannel.builder()
                                              .group(group)
                                              .channel(channel)
                                              .permission(channelItem.permission())
                                              .build();
                groupChannelRepository.save(gc);
            }
        }

        channelAccessService.invalidateWorkspaceCache(group.getWorkspace()
                                                           .getId());

        group = groupRepository.save(group);

        return toResponse(group);
    }

    private GroupResponse toResponse(Group group) {
        return new GroupResponse(
                group.getId(),
                group.getWorkspace()
                     .getId(),
                group.getName(),
                group.getCreatedAt()
        );
    }

    @Transactional
    public void deleteGroup(Long groupId) {
        Group group = groupRepository.findById(groupId)
                                     .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        group.setDeletedAt(Instant.now());
        groupRepository.save(group);
    }
}