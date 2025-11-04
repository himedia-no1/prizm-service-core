package run.prizm.core.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import run.prizm.core.admin.dto.AdminLoginRequest;
import run.prizm.core.admin.entity.Admin;
import run.prizm.core.admin.repository.AdminRepository;
import run.prizm.core.common.constant.ErrorCode;
import run.prizm.core.common.exception.AuthException;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public Admin login(AdminLoginRequest request) {
        Admin admin = adminRepository.getByLoginIdOrThrow(request.loginId());

        if (!passwordEncoder.matches(request.password(), admin.getPassword())) {
            throw new AuthException(ErrorCode.UNAUTHORIZED);
        }

        return admin;
    }
}