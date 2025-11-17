package run.prizm.core.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.properties.FrontendProperties;
import run.prizm.core.security.cookie.CookieService;
import run.prizm.core.security.cookie.CookieUtils;
import run.prizm.core.security.jwt.JwtService;
import run.prizm.core.storage.redis.RefreshTokenCacheRepository;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.repository.UserRepository;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final CookieService cookieService;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final UserRepository userRepository;
    private final RefreshTokenCacheRepository refreshTokenCacheRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FrontendProperties frontendProperties;

    private static final String LAST_PATH_KEY_PREFIX = "user:last_path:";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        User user = extractUser(authentication);

        handleLanguageSetting(request, response, user);

        String refreshToken = jwtService.generateRefreshToken();
        refreshTokenCacheRepository.save(refreshToken, user.getId(), "USER");
        cookieService.setRefreshToken(response, refreshToken);

        String inviteCode = CookieUtils.getCookieValue(request, HttpCookieOAuth2AuthorizationRequestRepository.INVITE_CODE_PARAM_COOKIE_NAME);

        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        String redirectUrl = buildRedirectUrl(inviteCode, user.getId());
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void handleLanguageSetting(HttpServletRequest request, HttpServletResponse response, User user) {
        if (user.getLanguage() == null) {
            String nextLocale = CookieUtils.getCookieValue(request, "NEXT_LOCALE");
            if (nextLocale != null && !nextLocale.isEmpty()) {
                try {
                    Language language = Language.valueOf(nextLocale.toUpperCase());
                    user.setLanguage(language);
                    userRepository.save(user);
                } catch (IllegalArgumentException e) {
                }
            }
        }

        Language userLanguage = user.getLanguage() != null ? user.getLanguage() : Language.EN;
        Cookie nextLocaleCookie = new Cookie("NEXT_LOCALE", userLanguage.name().toLowerCase());
        nextLocaleCookie.setPath("/");
        nextLocaleCookie.setMaxAge(365 * 24 * 60 * 60);
        nextLocaleCookie.setHttpOnly(false);
        response.addCookie(nextLocaleCookie);
    }

    private String buildRedirectUrl(String inviteCode, Long userId) {
        String redirectPath = determineRedirectPath(inviteCode, userId);
        String frontendUrl = frontendProperties.getUrl();
        return (frontendUrl != null && !frontendUrl.isEmpty())
                ? frontendUrl + redirectPath
                : redirectPath;
    }

    private String determineRedirectPath(String inviteCode, Long userId) {
        if (inviteCode != null && !inviteCode.isEmpty()) {
            return "/invite/" + inviteCode;
        }
        String lastPath = getLastPath(userId);
        return (lastPath != null && !lastPath.isEmpty()) ? lastPath : "/workspace";
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

    private String getLastPath(Long userId) {
        String key = LAST_PATH_KEY_PREFIX + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }
}
