package run.prizm.core.admin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import run.prizm.core.admin.entity.Admin;
import run.prizm.core.common.constant.ErrorCode;
import run.prizm.core.common.exception.AuthException;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByLoginId(String loginId);

    Optional<Admin> findByIdAndDeletedAtIsNull(Long id);

    default Admin getByLoginIdOrThrow(String loginId) {
        return findByLoginId(loginId)
                .orElseThrow(() -> new AuthException(ErrorCode.UNAUTHORIZED));
    }

    default Admin getActiveByIdOrThrow(Long id) {
        return findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new AuthException(ErrorCode.ADMIN_NOT_FOUND));
    }
}