package run.prizm.core.space.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import run.prizm.core.space.group.entity.GroupWorkspaceUser;
import run.prizm.core.space.workspace.entity.WorkspaceUser;

import java.util.List;

public interface GroupWorkspaceUserRepository extends JpaRepository<GroupWorkspaceUser, Long> {
    
    @Query("SELECT gwu FROM GroupWorkspaceUser gwu WHERE gwu.group.id = :groupId AND gwu.workspaceUser.deletedAt IS NULL")
    List<GroupWorkspaceUser> findByGroupIdAndWorkspaceUserDeletedAtIsNull(Long groupId);
    
    @Modifying
    @Query("DELETE FROM GroupWorkspaceUser gwu WHERE gwu.group.id = :groupId")
    void deleteByGroupId(Long groupId);
    
    @Query("SELECT gwu FROM GroupWorkspaceUser gwu WHERE gwu.workspaceUser = :workspaceUser AND gwu.group.deletedAt IS NULL")
    List<GroupWorkspaceUser> findByWorkspaceUserAndGroupDeletedAtIsNull(WorkspaceUser workspaceUser);
}