package run.prizm.core.space.channel.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.entity.ChannelWorkspaceUser;
import run.prizm.core.space.channel.repository.ChannelRepository;
import run.prizm.core.space.channel.repository.ChannelWorkspaceUserRepository;
import run.prizm.core.space.group.constraint.GroupChannelPermission;
import run.prizm.core.space.group.entity.GroupChannel;
import run.prizm.core.space.group.entity.GroupWorkspaceUser;
import run.prizm.core.space.group.repository.GroupChannelRepository;
import run.prizm.core.space.group.repository.GroupWorkspaceUserRepository;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.entity.WorkspaceUser;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChannelPermissionCalculator {

    private final ChannelRepository channelRepository;
    private final ChannelWorkspaceUserRepository channelWorkspaceUserRepository;
    private final GroupWorkspaceUserRepository groupWorkspaceUserRepository;
    private final GroupChannelRepository groupChannelRepository;

    public Map<Long, ChannelPermission> calculatePermissions(WorkspaceUser workspaceUser) {
        Long workspaceId = workspaceUser.getWorkspace()
                                        .getId();

        if (workspaceUser.getRole() == WorkspaceUserRole.OWNER ||
                workspaceUser.getRole() == WorkspaceUserRole.MANAGER) {
            return getAllChatChannels(workspaceId).stream()
                                                  .collect(Collectors.toMap(Channel::getId, ch -> ChannelPermission.MANAGE));
        }

        if (workspaceUser.getRole() == WorkspaceUserRole.GUEST) {
            return getGuestChannels(workspaceUser);
        }

        return getMemberChannels(workspaceUser);
    }

    private List<Channel> getAllChatChannels(Long workspaceId) {
        return channelRepository.findByWorkspaceIdAndTypeAndDeletedAtIsNull(
                workspaceId,
                run.prizm.core.space.channel.constraint.ChannelType.CHAT
        );
    }

    private Map<Long, ChannelPermission> getGuestChannels(WorkspaceUser workspaceUser) {
        List<ChannelWorkspaceUser> explicitChannels = channelWorkspaceUserRepository
                .findByWorkspaceUserAndExplicitTrue(workspaceUser);

        return explicitChannels.stream()
                               .filter(cwu -> cwu.getChannel()
                                                 .getDeletedAt() == null)
                               .collect(Collectors.toMap(
                                       cwu -> cwu.getChannel()
                                                 .getId(),
                                       cwu -> ChannelPermission.WRITE
                               ));
    }

    private Map<Long, ChannelPermission> getMemberChannels(WorkspaceUser workspaceUser) {
        List<GroupWorkspaceUser> userGroups = groupWorkspaceUserRepository
                .findByWorkspaceUserAndGroupDeletedAtIsNull(workspaceUser);

        Map<Long, ChannelPermission> permissions = new java.util.HashMap<>();

        for (GroupWorkspaceUser gwu : userGroups) {
            List<GroupChannel> groupChannels = groupChannelRepository
                    .findByGroupIdAndChannelDeletedAtIsNull(gwu.getGroup()
                                                               .getId());

            for (GroupChannel gc : groupChannels) {
                ChannelPermission currentPermission = permissions.getOrDefault(
                        gc.getChannel()
                          .getId(),
                        ChannelPermission.NONE
                );
                ChannelPermission newPermission = convertPermission(gc.getPermission());
                permissions.put(
                        gc.getChannel()
                          .getId(),
                        ChannelPermission.max(currentPermission, newPermission)
                );
            }
        }

        return permissions;
    }

    private ChannelPermission convertPermission(GroupChannelPermission groupPermission) {
        return switch (groupPermission) {
            case READ -> ChannelPermission.READ;
            case WRITE -> ChannelPermission.WRITE;
            case MANAGE -> ChannelPermission.MANAGE;
        };
    }
}