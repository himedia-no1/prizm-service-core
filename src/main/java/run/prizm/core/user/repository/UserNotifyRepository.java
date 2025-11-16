package run.prizm.core.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.user.entity.UserNotify;

public interface UserNotifyRepository extends JpaRepository<UserNotify, Long> {
}