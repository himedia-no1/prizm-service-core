package run.prizm.core.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import run.prizm.core.security.cookie.CookieUtils;

import java.io.IOException;

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
        String inviteCode = CookieUtils.getCookieValue(request, HttpCookieOAuth2AuthorizationRequestRepository.INVITE_CODE_PARAM_COOKIE_NAME);

        // 쿠키 정리
        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        // 리다이렉트 경로 결정
        String redirectPath = inviteCode != null && !inviteCode.isEmpty()
                ? "/invite/" + inviteCode
                : "/login";

        // 프론트엔드로 리다이렉트 (frontendUrl이 있으면 붙이기)
        String redirectUrl = (frontendUrl != null && !frontendUrl.isEmpty())
                ? frontendUrl + redirectPath
                : redirectPath;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}