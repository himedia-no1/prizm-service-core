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
import run.prizm.core.security.cookie.CookieService;
import run.prizm.core.security.jwt.JwtService;
import run.prizm.core.storage.redis.RefreshTokenCacheRepository;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.repository.UserRepository;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final CookieService cookieService;
    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final UserRepository userRepository;
    private final RefreshTokenCacheRepository refreshTokenCacheRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LAST_PATH_KEY_PREFIX = "user:last_path:";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        User user = extractUser(authentication);

        // NEXT_LOCALE 쿠키에서 언어 설정 읽기 및 저장
        String nextLocale = getCookieValue(request, "NEXT_LOCALE");
        if (nextLocale != null && !nextLocale.isEmpty()) {
            try {
                Language language = Language.valueOf(nextLocale.toUpperCase());
                user.setLanguage(language);
                userRepository.save(user);
            } catch (IllegalArgumentException e) {
                // 잘못된 언어 코드는 무시
            }
        }

        // NEXT_LOCALE 쿠키를 유저의 현재 언어로 업데이트하여 브라우저에 전달
        Language userLanguage = user.getLanguage() != null ? user.getLanguage() : Language.EN;
        Cookie nextLocaleCookie = new Cookie("NEXT_LOCALE", userLanguage.name().toLowerCase());
        nextLocaleCookie.setPath("/");
        nextLocaleCookie.setMaxAge(365 * 24 * 60 * 60); // 1년
        nextLocaleCookie.setHttpOnly(false); // JavaScript에서 읽을 수 있어야 함
        response.addCookie(nextLocaleCookie);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken();

        refreshTokenCacheRepository.save(refreshToken, user.getId(), "USER");
        cookieService.setRefreshToken(response, refreshToken);

        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        // 마지막 접속 경로 조회 (없으면 null)
        String lastPath = getLastPath(user.getId());

        // JSON 응답
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter writer = response.getWriter();
        writer.write("{");
        writer.write("\"success\":true,");
        writer.write("\"accessToken\":\"" + accessToken + "\",");
        if (lastPath != null && !lastPath.isEmpty()) {
            writer.write("\"redirectPath\":\"" + escapeJson(lastPath) + "\"");
        } else {
            writer.write("\"redirectPath\":null");
        }
        writer.write("}");
        writer.flush();
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

    private String getLastPath(Long userId) {
        String key = LAST_PATH_KEY_PREFIX + userId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
}