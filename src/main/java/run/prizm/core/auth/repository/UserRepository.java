package run.prizm.core.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.core.auth.entity.User;
import run.prizm.core.auth.entity.UserAuthProvider;

import java.util.Optional;
import java.util.UUID;



public interface UserRepository extends JpaRepository<User, Long> {



    Optional<User> findByAuthProviderAndOpenidSub(UserAuthProvider authProvider, String openidSub);

}
