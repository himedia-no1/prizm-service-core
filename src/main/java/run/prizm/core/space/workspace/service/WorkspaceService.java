package run.prizm.core.space.workspace.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.common.util.ImageUploadHelper;
import run.prizm.core.file.entity.File;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.repository.UserRepository;
import run.prizm.core.space.workspace.constraint.WorkspaceUserNotify;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.constraint.WorkspaceUserState;
import run.prizm.core.space.workspace.dto.WorkspaceCreateRequest;
import run.prizm.core.space.workspace.dto.WorkspaceListItemResponse;
import run.prizm.core.space.workspace.dto.WorkspaceResponse;
import run.prizm.core.space.workspace.dto.WorkspaceUpdateRequest;
import run.prizm.core.space.workspace.entity.Workspace;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.repository.WorkspaceRepository;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceUserRepository workspaceUserRepository;
    private final UserRepository userRepository;
    private final ImageUploadHelper imageUploadHelper;

    @Transactional
    public WorkspaceResponse createWorkspace(Long userId, WorkspaceCreateRequest request) {
        User owner = userRepository.findById(userId)
                                   .orElseThrow(() -> new RuntimeException("User not found"));

        Workspace workspace = Workspace.builder()
                                       .name(request.name())
                                       .image(null)
                                       .build();
        workspaceRepository.save(workspace);

        workspaceUserRepository.save(
                WorkspaceUser.builder()
                             .workspace(workspace)
                             .user(owner)
                             .role(WorkspaceUserRole.OWNER)
                             .image(owner.getImage())
                             .name(null)

                             .state(WorkspaceUserState.ONLINE)
                             .notify(WorkspaceUserNotify.ON)
                             .banned(false)
                             .build()
        );

        String imageUrl = imageUploadHelper.getImageUrl(workspace.getImage());
        return new WorkspaceResponse(workspace.getId(), workspace.getName(), imageUrl, workspace.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public WorkspaceResponse getWorkspace(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        String imageUrl = imageUploadHelper.getImageUrl(workspace.getImage());
        return new WorkspaceResponse(workspace.getId(), workspace.getName(), imageUrl, workspace.getCreatedAt());
    }

    @Transactional
    public WorkspaceResponse updateWorkspace(Long workspaceId, WorkspaceUpdateRequest request) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        if (request.image() != null && !request.image().isEmpty()) {
            if (workspace.getImage() != null) {
                imageUploadHelper.deleteImage(workspace.getImage());
            }
            
            File newImage = imageUploadHelper.uploadImage(request.image(), "workspaces");
            workspace.setImage(newImage);
        }

        if (request.name() != null) {
            workspace.setName(request.name());
        }

        workspaceRepository.save(workspace);
        return getWorkspace(workspaceId);
    }

    @Transactional
    public void deleteWorkspace(Long workspaceId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new RuntimeException("Workspace not found"));

        workspace.setDeletedAt(Instant.now());
        workspaceRepository.save(workspace);
    }

    @Transactional(readOnly = true)
    public List<WorkspaceListItemResponse> listUserWorkspaces(Long userId) {
        return workspaceUserRepository.findActiveWorkspacesByUserId(userId).stream()
                .map(wu -> {
                    Workspace workspace = wu.getWorkspace();
                    String imageUrl = workspace.getImage() != null 
                            ? imageUploadHelper.getImageUrl(workspace.getImage())
                            : null;
                    return new WorkspaceListItemResponse(
                            workspace.getId(),
                            workspace.getName(),
                            imageUrl
                    );
                })
                .toList();
    }
}
