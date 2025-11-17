package run.prizm.core.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.security.cookie.CookieService;
import run.prizm.core.security.jwt.JwtService;
import run.prizm.core.storage.redis.RefreshTokenCacheRepository;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.repository.UserRepository;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final CookieService cookieService;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final UserRepository userRepository;
    private final RefreshTokenCacheRepository refreshTokenCacheRepository;

    @Value("${prizm.frontend.url:}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        User user = extractUser(authentication);

        String languageParam = getCookieValue(request, HttpCookieOAuth2AuthorizationRequestRepository.LANGUAGE_PARAM_COOKIE_NAME);
        String inviteCode = getCookieValue(request, HttpCookieOAuth2AuthorizationRequestRepository.INVITE_CODE_PARAM_COOKIE_NAME);

        if (languageParam != null && !languageParam.isEmpty()) {
            try {
                Language language = Language.valueOf(languageParam.toUpperCase());
                user.setLanguage(language);
                userRepository.save(user);
            } catch (IllegalArgumentException e) {
                // ignore invalid language code
            }
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken();

        refreshTokenCacheRepository.save(refreshToken, user.getId(), "USER");
        cookieService.setAccessToken(response, accessToken);
        cookieService.setRefreshToken(response, refreshToken);

        String path = inviteCode != null && !inviteCode.isEmpty()
                ? "/invite/" + inviteCode
                : "/login";

        String redirectUrl = (frontendUrl != null && !frontendUrl.isEmpty())
                ? frontendUrl + path
                : path;

        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private User extractUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomOAuth2User customUser) {
            return customUser.getUser();
        }
        if (principal instanceof OidcUser) {
            throw new IllegalStateException("Received default OidcUser instead of CustomOAuth2User. Check CustomOAuth2UserService configuration.");
        }
        throw new IllegalStateException("Unsupported authentication principal type: " + principal.getClass());
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> name.equals(cookie.getName()))
                    .findFirst()
                    .map(Cookie::getValue)
                    .orElse(null);
        }
        return null;
    }
}