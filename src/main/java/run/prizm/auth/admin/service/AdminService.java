package run.prizm.auth.admin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import run.prizm.auth.admin.dto.AdminLoginRequest;
import run.prizm.auth.admin.entity.Admin;
import run.prizm.auth.admin.repository.AdminRepository;
import run.prizm.auth.common.constant.ErrorCode;
import run.prizm.auth.common.exception.AuthException;

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