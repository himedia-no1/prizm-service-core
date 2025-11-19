package run.prizm.core.space.channel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.common.util.ZIndexCalculator;
import run.prizm.core.space.category.entity.Category;
import run.prizm.core.space.category.repository.CategoryRepository;
import run.prizm.core.space.channel.constraint.ChannelWorkspaceUserNotify;
import run.prizm.core.space.channel.dto.*;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.entity.ChannelWorkspaceUser;
import run.prizm.core.space.channel.repository.ChannelRepository;
import run.prizm.core.space.channel.repository.ChannelWorkspaceUserRepository;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.repository.WorkspaceRepository;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CategoryRepository categoryRepository;
    private final ChannelWorkspaceUserRepository channelWorkspaceUserRepository;
    private final WorkspaceUserRepository workspaceUserRepository;

    @Transactional
    public ChannelResponse createChannel(Long workspaceId, Long categoryId, ChannelCreateRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                                                 .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_NOT_FOUND));

        Category category = categoryRepository.findById(categoryId)
                                              .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        BigDecimal zIndex = channelRepository.findFirstByCategoryIdAndDeletedAtIsNullOrderByZIndexDesc(categoryId)
                                             .map(lastChannel -> lastChannel.getZIndex()
                                                                            .add(BigDecimal.ONE))
                                             .orElse(BigDecimal.ONE);

        Channel channel = Channel.builder()
                                 .workspace(workspace)
                                 .category(category)
                                 .type(request.type())
                                 .name(request.name())
                                 .description(request.description())
                                 .zIndex(zIndex)
                                 .build();

        channel = channelRepository.save(channel);

        return toResponse(channel);
    }

    @Transactional(readOnly = true)
    public ChannelInfoResponse getChannelInfo(Long workspaceId, Long channelId, Long userId) {
        Channel channel = channelRepository.findById(channelId)
                                           .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        WorkspaceUser workspaceUser = workspaceUserRepository
                .findByWorkspaceIdAndUserIdAndDeletedAtIsNull(workspaceId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        ChannelWorkspaceUserNotify notify = channelWorkspaceUserRepository
                .findByChannelAndWorkspaceUser(channel, workspaceUser)
                .map(ChannelWorkspaceUser::getNotify)
                .orElse(ChannelWorkspaceUserNotify.ON);

        return new ChannelInfoResponse(
                channel.getId(),
                channel.getName(),
                channel.getDescription(),
                notify
        );
    }

    @Transactional
    public ChannelResponse updateChannel(Long channelId, ChannelUpdateRequest request) {
        Channel channel = channelRepository.findById(channelId)
                                           .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        if (request.name() != null) {
            channel.setName(request.name());
        }

        if (request.description() != null) {
            channel.setDescription(request.description());
        }

        channel = channelRepository.save(channel);

        return toResponse(channel);
    }

    @Transactional
    public void updateZIndex(Long channelId, ChannelZIndexUpdateRequest request) {
        Channel channel = channelRepository.findById(channelId)
                                           .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        List<Channel> channels = channelRepository
                .findByCategoryIdAndDeletedAtIsNullOrderByZIndex(channel.getCategory()
                                                                        .getId());

        BigDecimal newZIndex;

        if ("FIRST".equals(request.position())) {
            Channel firstChannel = channels.get(0);
            newZIndex = ZIndexCalculator.calculateFirst(firstChannel.getZIndex());
        } else if ("LAST".equals(request.position())) {
            Channel lastChannel = channels.get(channels.size() - 1);
            newZIndex = ZIndexCalculator.calculateLast(lastChannel.getZIndex());
        } else if ("BETWEEN".equals(request.position())) {
            Channel beforeChannel = channelRepository.findById(request.beforeId())
                                                     .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
            Channel afterChannel = channelRepository.findById(request.afterId())
                                                    .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
            newZIndex = ZIndexCalculator.calculateBetween(beforeChannel.getZIndex(), afterChannel.getZIndex());
        } else {
            throw new BusinessException(ErrorCode.INVALID_POSITION);
        }

        channel.setZIndex(newZIndex);
        channelRepository.save(channel);
    }

    @Transactional
    public void updateNotify(Long channelId, Long workspaceUserId, ChannelNotifyUpdateRequest request) {
        Channel channel = channelRepository.findById(channelId)
                                           .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));
        WorkspaceUser workspaceUser = workspaceUserRepository.findById(workspaceUserId)
                                                             .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        channelWorkspaceUserRepository.findByChannelAndWorkspaceUser(channel, workspaceUser)
                                      .ifPresentOrElse(
                                              cwu -> {
                                                  cwu.setNotify(request.notifyType());
                                                  channelWorkspaceUserRepository.save(cwu);
                                              },
                                              () -> {
                                                  ChannelWorkspaceUser cwu = ChannelWorkspaceUser.builder()
                                                                                                 .channel(channel)
                                                                                                 .workspaceUser(workspaceUser)
                                                                                                 .explicit(false)
                                                                                                 .notify(request.notifyType())
                                                                                                 .build();
                                                  channelWorkspaceUserRepository.save(cwu);
                                              }
                                      );
    }

    @Transactional
    public void deleteChannel(Long channelId) {
        Channel channel = channelRepository.findById(channelId)
                                           .orElseThrow(() -> new BusinessException(ErrorCode.CHANNEL_NOT_FOUND));

        channel.setDeletedAt(Instant.now());
        channelRepository.save(channel);
    }

    private ChannelResponse toResponse(Channel channel) {
        return new ChannelResponse(
                channel.getId(),
                channel.getWorkspace()
                       .getId(),
                channel.getCategory() != null ? channel.getCategory()
                                                       .getId() : null,
                channel.getType(),
                channel.getName(),
                channel.getDescription(),
                channel.getZIndex()
                       .toPlainString(),
                channel.getCreatedAt()
        );
    }
}