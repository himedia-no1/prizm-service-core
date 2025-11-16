package run.prizm.core.security.oauth2;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    @Value("${prizm.frontend.url:}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.error("OAuth2 authentication failed", exception);
        
        // 쿠키에서 초대코드 가져오기
        String inviteCode = getCookieValue(request, HttpCookieOAuth2AuthorizationRequestRepository.INVITE_CODE_PARAM_COOKIE_NAME);
        
        // 쿠키 정리
        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
        
        String path = inviteCode != null && !inviteCode.isEmpty()
                ? "/invite/" + inviteCode
                : "/login";
        
        String redirectUrl = (frontendUrl != null && !frontendUrl.isEmpty())
                ? frontendUrl + path
                : path;
        
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
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