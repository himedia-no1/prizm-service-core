package run.prizm.core.space.group.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import run.prizm.core.space.group.entity.GroupChannel;

import java.util.List;

public interface GroupChannelRepository extends JpaRepository<GroupChannel, Long> {

    @Query("SELECT gc FROM GroupChannel gc WHERE gc.group.id = :groupId AND gc.channel.deletedAt IS NULL")
    List<GroupChannel> findByGroupIdAndChannelDeletedAtIsNull(Long groupId);

    @Modifying
    @Query("DELETE FROM GroupChannel gc WHERE gc.group.id = :groupId")
    void deleteByGroupId(Long groupId);
}