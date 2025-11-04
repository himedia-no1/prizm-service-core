package run.prizm.core.admin.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.prizm.core.admin.dto.AdminLoginRequest;
import run.prizm.core.admin.entity.Admin;
import run.prizm.core.admin.service.AdminService;
import run.prizm.core.admin.service.token.AdminTokenService;
import run.prizm.core.common.constant.HttpConstants;
import run.prizm.core.common.dto.Token;
import run.prizm.core.common.util.CookieUtil;

@RestController
@RequestMapping("/auth/admin")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminService adminService;
    private final AdminTokenService adminTokenService;
    private final CookieUtil cookieUtil;

    @PostMapping("/login")
    public ResponseEntity<Void> login(@Valid @RequestBody AdminLoginRequest request, HttpServletResponse response) {
        Admin admin = adminService.login(request);

        Token tokenDto = adminTokenService.generateToken(admin);

        response.setHeader(HttpHeaders.AUTHORIZATION, tokenDto.tokenType() + " " + tokenDto.accessToken());
        String cookieHeader = cookieUtil.buildSetCookieHeader(
                HttpConstants.COOKIE_REFRESH_TOKEN.getValue(),
                tokenDto.refreshToken(),
                cookieUtil.getRefreshTokenMaxAge(),
                HttpConstants.PATH_ROOT.getValue()
        );
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);

        return ResponseEntity.ok()
                             .build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletResponse response
    ) {
        Token tokenDto = adminTokenService.refreshAccessToken(refreshToken);

        response.setHeader(HttpHeaders.AUTHORIZATION, tokenDto.tokenType() + " " + tokenDto.accessToken());
        String cookieHeader = cookieUtil.buildSetCookieHeader(
                HttpConstants.COOKIE_REFRESH_TOKEN.getValue(),
                tokenDto.refreshToken(),
                cookieUtil.getRefreshTokenMaxAge(),
                HttpConstants.PATH_ROOT.getValue()
        );
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);

        return ResponseEntity.ok()
                             .build();
    }
}