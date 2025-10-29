package run.prizm.auth.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import run.prizm.auth.common.constant.ErrorCode;
import run.prizm.auth.common.exception.AuthException;
import run.prizm.auth.user.entity.User;
import run.prizm.auth.user.entity.UserAuthProvider;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUuid(UUID uuid);

    default User getActiveByUuidOrThrow(UUID uuid) {
        return findByUuid(uuid)
                .orElseThrow(() -> new AuthException(ErrorCode.USER_NOT_FOUND));
    }

    Optional<User> findByAuthProviderAndOpenidSub(UserAuthProvider authProvider, String openidSub);
}
