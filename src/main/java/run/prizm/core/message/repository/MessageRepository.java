package run.prizm.core.message.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import run.prizm.core.message.entity.Message;
import run.prizm.core.space.channel.entity.Channel;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {
    long countByChannel(Channel channel);
    
    @Query("SELECT m FROM Message m " +
           "WHERE m.channel.id = :channelId " +
           "ORDER BY m.createdAt DESC")
    List<Message> findByChannelIdOrderByCreatedAtDesc(
            @Param("channelId") Long channelId, 
            Pageable pageable);
    
    @Query("SELECT m FROM Message m " +
           "LEFT JOIN FETCH m.channel " +
           "LEFT JOIN FETCH m.workspaceUser wu " +
           "LEFT JOIN FETCH wu.user " +
           "LEFT JOIN FETCH m.file " +
           "LEFT JOIN FETCH m.reply " +
           "LEFT JOIN FETCH m.thread " +
           "WHERE m.id = :id")
    Optional<Message> findByIdWithRelations(@Param("id") Long id);
}