package run.prizm.core.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.user.constraint.UserAuthProvider;
import run.prizm.core.user.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByAuthProviderAndOpenidSub(UserAuthProvider userAuthProvider, String openidSub);
    
    Optional<User> findByAuthProviderAndOpenidSubAndDeletedAtIsNull(UserAuthProvider userAuthProvider, String openidSub);
}