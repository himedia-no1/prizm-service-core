package run.prizm.core.space.workspace.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import run.prizm.core.space.workspace.constraint.WorkspaceUserRole;
import run.prizm.core.space.workspace.entity.WorkspaceUser;

import java.util.List;
import java.util.Optional;

public interface WorkspaceUserRepository extends JpaRepository<WorkspaceUser, Long> {

    boolean existsByWorkspaceIdAndUserIdAndDeletedAtIsNull(Long workspaceId, Long userId);

    long countByWorkspaceIdAndDeletedAtIsNull(Long workspaceId);

    List<WorkspaceUser> findByWorkspaceIdAndDeletedAtIsNull(Long workspaceId);

    List<WorkspaceUser> findByWorkspaceIdAndRoleAndDeletedAtIsNull(Long workspaceId, WorkspaceUserRole role);

    Optional<WorkspaceUser> findByWorkspaceIdAndUserIdAndDeletedAtIsNull(Long workspaceId, Long userId);

    Optional<WorkspaceUser> findByWorkspaceIdAndUserId(Long workspaceId, Long userId);

    @Query("SELECT wu FROM WorkspaceUser wu " +
            "JOIN FETCH wu.workspace w " +
            "WHERE wu.user.id = :userId " +
            "AND wu.deletedAt IS NULL " +
            "AND w.deletedAt IS NULL " +
            "ORDER BY w.name ASC")
    List<WorkspaceUser> findActiveWorkspacesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT wu FROM WorkspaceUser wu " +
            "LEFT JOIN FETCH wu.user u " +
            "LEFT JOIN FETCH wu.workspace w " +
            "WHERE wu.id = :id")
    Optional<WorkspaceUser> findByIdWithUser(@Param("id") Long id);
}