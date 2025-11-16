package run.prizm.core.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.prizm.core.user.service.UserService;
import run.prizm.core.security.cookie.CookieService;
import run.prizm.core.security.jwt.Token;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final CookieService cookieService;

    @PostMapping("/refresh")
    public ResponseEntity<Void> refresh(
            @CookieValue(name = "refresh_token") String refreshToken,
            HttpServletResponse response
    ) {
        Token token = userService.refreshAccessToken(refreshToken);
        response.setHeader(HttpHeaders.AUTHORIZATION, token.tokenType() + " " + token.accessToken());
        cookieService.setRefreshToken(response, token.refreshToken());
        return ResponseEntity.ok().build();
    }
}