package run.prizm.core.space.workspace.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.repository.UserRepository;
import run.prizm.core.space.workspace.constraint.WorkspaceUserNotify;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.constraint.WorkspaceUserState;
import run.prizm.core.file.entity.File;
import run.prizm.core.file.repository.FileRepository;
import run.prizm.core.space.workspace.dto.WorkspaceCreateRequest;
import run.prizm.core.space.workspace.dto.WorkspaceResponse;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.repository.WorkspaceRepository;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Transactional
    public WorkspaceResponse createWorkspace(Long userId, WorkspaceCreateRequest request) {
        User owner = userRepository.findById(userId)
                                   .orElseThrow(() -> new RuntimeException("User not found"));

        File image = null;
        if (request.imageId() != null) {
            image = fileRepository.findById(request.imageId())
                                  .orElseThrow(() -> new RuntimeException("Workspace image not found"));
        }

        Workspace workspace = Workspace.builder()
                                       .name(request.name())
                                       .image(image)
                                       .build();
        workspaceRepository.save(workspace);

        workspaceUserRepository.save(
                WorkspaceUser.builder()
                             .workspace(workspace)
                             .user(owner)
                             .role(WorkspaceUserRole.OWNER)
                             .image(owner.getImage())
                             .name(owner.getName())
                             .email(owner.getEmail())
                             .state(WorkspaceUserState.ONLINE)
                             .notify(WorkspaceUserNotify.ON)
                             .banned(false)
                             .build()
        );

        // TODO: replace with actual virtual assistant user reference when available
        workspaceUserRepository.save(
                WorkspaceUser.builder()
                             .workspace(workspace)
                             .user(owner)
                             .role(WorkspaceUserRole.OWNER)
                             .image(owner.getImage())
                             .name(owner.getName())
                             .email(owner.getEmail())
                             .state(WorkspaceUserState.ONLINE)
                             .notify(WorkspaceUserNotify.ON)
                             .banned(false)
                             .build()
        );

        Long responseImageId = workspace.getImage() != null ? workspace.getImage()
                                                                    .getId() : null;
        return new WorkspaceResponse(workspace.getId(), workspace.getName(), responseImageId);
    }
}