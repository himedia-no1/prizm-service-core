package run.prizm.core.space.airag.permission;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.entity.WorkspaceUser;
import run.prizm.core.space.workspace.repository.WorkspaceUserRepository;

@Component
@RequiredArgsConstructor
public class AiRagPermissionChecker {

    private final WorkspaceUserRepository workspaceUserRepository;

    /**
     * AI RAG 관리 권한 체크 (OWNER, MANAGER만 가능)
     */
    public void checkAiRagManagePermission(Long workspaceUserId) {
        WorkspaceUser workspaceUser = workspaceUserRepository.findById(workspaceUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        WorkspaceUserRole role = workspaceUser.getRole();

        if (role != WorkspaceUserRole.OWNER && role != WorkspaceUserRole.MANAGER) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "Only OWNER and MANAGER can manage AI RAG files"
            );
        }
    }

    /**
     * AI 어시스턴트 사용 권한 체크 (OWNER, MANAGER, MEMBER만 가능)
     */
    public void checkAiAssistantPermission(Long workspaceUserId) {
        WorkspaceUser workspaceUser = workspaceUserRepository.findById(workspaceUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));

        WorkspaceUserRole role = workspaceUser.getRole();

        if (role == WorkspaceUserRole.GUEST) {
            throw new BusinessException(
                    ErrorCode.FORBIDDEN,
                    "GUEST users cannot use AI Assistant"
            );
        }
    }

    /**
     * WorkspaceUser 조회 및 역할 반환
     */
    public WorkspaceUser getWorkspaceUserWithPermission(Long workspaceUserId) {
        return workspaceUserRepository.findById(workspaceUserId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WORKSPACE_USER_NOT_FOUND));
    }
}
