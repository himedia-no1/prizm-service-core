package run.prizm.core.space.channel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import run.prizm.core.space.channel.constraint.ChannelType;
import run.prizm.core.space.channel.entity.Channel;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    @Query("SELECT c FROM Channel c WHERE c.category.id = :categoryId AND c.deletedAt IS NULL ORDER BY c.zIndex ASC")
    List<Channel> findByCategoryIdAndDeletedAtIsNullOrderByZIndex(Long categoryId);

    @Query("SELECT c FROM Channel c WHERE c.category.id = :categoryId AND c.deletedAt IS NULL ORDER BY c.zIndex DESC LIMIT 1")
    Optional<Channel> findFirstByCategoryIdAndDeletedAtIsNullOrderByZIndexDesc(Long categoryId);

    List<Channel> findByWorkspaceIdAndTypeAndDeletedAtIsNull(Long workspaceId, ChannelType type);
}