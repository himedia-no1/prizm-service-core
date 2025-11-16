package run.prizm.core.space.group.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.space.group.dto.GroupCreateRequest;
import run.prizm.core.space.group.entity.Group;
import run.prizm.core.space.group.repository.GroupRepository;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.repository.WorkspaceRepository;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final WorkspaceRepository workspaceRepository;

    @Transactional
    public Group createGroup(Long workspaceId, GroupCreateRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                                                 .orElseThrow(() -> new RuntimeException("Workspace not found"));

        Group group = Group.builder()
                           .workspace(workspace)
                           .name(request.name())
                           .build();

        return groupRepository.save(group);
    }
}