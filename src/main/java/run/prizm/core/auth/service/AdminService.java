package run.prizm.core.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import run.prizm.core.auth.dto.AdminLoginRequest;
import run.prizm.core.auth.domain.Admin;
import run.prizm.core.auth.repository.AdminRepository;
import run.prizm.core.auth.constant.ErrorCode;
import run.prizm.core.auth.exception.AuthException;

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