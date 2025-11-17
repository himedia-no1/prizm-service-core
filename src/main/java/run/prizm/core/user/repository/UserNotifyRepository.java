package run.prizm.core.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.user.entity.UserNotify;

import java.util.List;

public interface UserNotifyRepository extends JpaRepository<UserNotify, Long> {

    List<UserNotify> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
}