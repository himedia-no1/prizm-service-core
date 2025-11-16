package run.prizm.core.space.channel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.space.channel.dto.ChannelCreateRequest;
import run.prizm.core.space.channel.entity.Channel;
import run.prizm.core.space.channel.repository.ChannelRepository;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.repository.WorkspaceRepository;
import run.prizm.core.space.category.entity.Category;
import run.prizm.core.space.category.repository.CategoryRepository;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;
    private final WorkspaceRepository workspaceRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public Channel createChannel(Long workspaceId, Long categoryId, ChannelCreateRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                                                 .orElseThrow(() -> new RuntimeException("Workspace not found"));

        Category category = categoryRepository.findById(categoryId)
                                              .orElseThrow(() -> new RuntimeException("Category not found"));

        if (!category.getWorkspace()
                     .getId()
                     .equals(workspace.getId())) {
            throw new RuntimeException("Category does not belong to workspace");
        }

        Channel channel = Channel.builder()
                                 .workspace(workspace)
                                 .category(category)
                                 .type(request.type())
                                 .name(request.name())
                                 .description(request.description())
                                 .zIndex(0D)
                                 .build();

        return channelRepository.save(channel);
    }
}