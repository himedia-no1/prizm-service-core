package run.prizm.core.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.properties.UrlProperties;
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

    private static final String NEXT_LOCALE_COOKIE = "NEXT_LOCALE";
    private static final String DEFAULT_ROLE = "USER";
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final UserRepository userRepository;
    private final RefreshTokenCacheRepository refreshTokenCacheRepository;
    private final UrlProperties urlProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        UserContext userContext = extractUser(authentication);
        User user = userContext.user();

        handleLanguageSetting(request, response, userContext);

        String refreshToken = jwtService.generateRefreshToken();
        String role = resolveRole(user);
        refreshTokenCacheRepository.save(refreshToken, user.getId(), role);
        cookieService.setRefreshToken(response, refreshToken);

        String inviteCode = CookieUtils.getCookieValue(request, HttpCookieOAuth2AuthorizationRequestRepository.INVITE_CODE_PARAM_COOKIE_NAME);

        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        String redirectUrl = buildRedirectUrl(inviteCode);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void handleLanguageSetting(HttpServletRequest request, HttpServletResponse response, UserContext userContext) {
        User user = userContext.user();
        String nextLocale = CookieUtils.getCookieValue(request, NEXT_LOCALE_COOKIE);
        Language requestedLanguage = parseLanguage(nextLocale);
        boolean updated = false;

        if (userContext.isNewUser() && requestedLanguage != null && user.getLanguage() != requestedLanguage) {
            user.setLanguage(requestedLanguage);
            updated = true;
        }

        if (user.getLanguage() == null) {
            Language fallbackLanguage = requestedLanguage != null ? requestedLanguage : Language.EN;
            user.setLanguage(fallbackLanguage);
            updated = true;
        }

        if (updated) {
            userRepository.save(user);
        }

        Language userLanguage = user.getLanguage() != null ? user.getLanguage() : Language.EN;
        Cookie nextLocaleCookie = new Cookie(NEXT_LOCALE_COOKIE, userLanguage.name()
                                                                             .toLowerCase());
        nextLocaleCookie.setPath("/");
        nextLocaleCookie.setMaxAge(365 * 24 * 60 * 60);
        nextLocaleCookie.setHttpOnly(false);
        response.addCookie(nextLocaleCookie);
    }

    private String buildRedirectUrl(String inviteCode) {
        String redirectPath = determineRedirectPath(inviteCode);
        String frontendUrl = urlProperties.getWebUserUrl();
        return (frontendUrl != null && !frontendUrl.isEmpty())
                ? frontendUrl + redirectPath
                : redirectPath;
    }

    private String determineRedirectPath(String inviteCode) {
        if (inviteCode != null && !inviteCode.isEmpty()) {
            return "/invite/" + inviteCode;
        }
        return "/login";
    }

    private UserContext extractUser(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomOAuth2User customUser) {
            return new UserContext(customUser.getUser(), customUser.isNewUser());
        }
        if (principal instanceof OidcUser) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private Language parseLanguage(String languageParam) {
        if (languageParam == null || languageParam.isEmpty()) {
            return null;
        }
        try {
            return Language.from(languageParam);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String resolveRole(User user) {
        return DEFAULT_ROLE;
    }

    private record UserContext(User user, boolean isNewUser) {
    }
}
