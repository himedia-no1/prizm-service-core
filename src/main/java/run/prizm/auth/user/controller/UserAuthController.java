package run.prizm.auth.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.prizm.auth.common.constant.HttpConstants;
import run.prizm.auth.common.dto.Token;
import run.prizm.auth.common.util.CookieUtil;
import run.prizm.auth.user.service.token.UserTokenService;

@RestController
@RequestMapping("/auth/user")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserTokenService userTokenService;
    private final CookieUtil cookieUtil;

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletResponse response
    ) {
        Token tokenDto = userTokenService.refreshAccessToken(refreshToken);

        response.setHeader(HttpHeaders.AUTHORIZATION, tokenDto.tokenType() + " " + tokenDto.accessToken());
        String cookieHeader = cookieUtil.buildSetCookieHeader(
            HttpConstants.COOKIE_REFRESH_TOKEN.getValue(),
            tokenDto.refreshToken(),
            cookieUtil.getRefreshTokenMaxAge(),
            HttpConstants.PATH_ROOT.getValue()
        );
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);
        
        return ResponseEntity.ok().build();
    }
}
