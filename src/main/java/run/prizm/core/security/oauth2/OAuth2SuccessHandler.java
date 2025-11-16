package run.prizm.core.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import run.prizm.core.user.entity.User;
import run.prizm.core.security.cookie.CookieService;
import run.prizm.core.security.jwt.JwtService;
import run.prizm.core.security.jwt.Token;

import java.io.IOException;
import java.lang.reflect.Method;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final CookieService cookieService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        User user = extractUser(authentication);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        long expiresIn = jwtService.getAccessTokenExpirationInSeconds();
        Token token = new Token(accessToken, refreshToken, expiresIn);

        response.setHeader(HttpHeaders.AUTHORIZATION, token.tokenType() + " " + token.accessToken());
        cookieService.setRefreshToken(response, token.refreshToken());

        getRedirectStrategy().sendRedirect(request, response, "/login");
    }

    private User extractUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OAuth2User) {
            try {
                Method getUserMethod = principal.getClass()
                                                .getMethod("getUser");
                return (User) getUserMethod.invoke(principal);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to extract User from OAuth2User", e);
            }
        }
        throw new IllegalStateException("Unsupported authentication principal type: " + principal.getClass());
    }
}