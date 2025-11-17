package run.prizm.core.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import run.prizm.core.properties.FrontendProperties;
import run.prizm.core.security.cookie.CookieUtils;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final HttpCookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;
    private final FrontendProperties frontendProperties;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        log.error("OAuth2 authentication failed", exception);

        String inviteCode = CookieUtils.getCookieValue(request, HttpCookieOAuth2AuthorizationRequestRepository.INVITE_CODE_PARAM_COOKIE_NAME);

        cookieAuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        String redirectPath = inviteCode != null && !inviteCode.isEmpty()
                ? "/invite/" + inviteCode
                : "/login";

        String frontendUrl = frontendProperties.getRedirectUrl();
        String redirectUrl = (frontendUrl != null && !frontendUrl.isEmpty())
                ? frontendUrl + redirectPath
                : redirectPath;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}