package run.prizm.core.auth.service.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import run.prizm.core.config.AuthConfigProperties;
import run.prizm.core.auth.constant.ApiEndpoint;
import run.prizm.core.auth.constant.HttpConstants;
import run.prizm.core.auth.dto.Token;
import run.prizm.core.auth.security.oauth2.CustomOAuth2User;
import run.prizm.core.auth.util.CookieUtil;
import run.prizm.core.auth.service.UserTokenService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserTokenService userTokenService;
    private final CookieUtil cookieUtil;
    private final AuthConfigProperties authConfigProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomOAuth2User oAuth2User = resolvePrincipal(authentication);

        Token tokenDto = userTokenService.generateToken(oAuth2User.getUser());

        response.setHeader(HttpHeaders.AUTHORIZATION, tokenDto.tokenType() + " " + tokenDto.accessToken());
        String cookieHeader = cookieUtil.buildSetCookieHeader(
                HttpConstants.COOKIE_REFRESH_TOKEN.getValue(),
                tokenDto.refreshToken(),
                cookieUtil.getRefreshTokenMaxAge(),
                HttpConstants.PATH_ROOT.getValue()
        );
        response.addHeader(HttpHeaders.SET_COOKIE, cookieHeader);

        String targetUrl = UriComponentsBuilder
                .fromUriString(authConfigProperties.getApp()
                                                   .getFrontendUserUrl())
                .path(ApiEndpoint.FRONTEND_LOGIN_SUCCESS.getPath())
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private CustomOAuth2User resolvePrincipal(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User;
        }
        throw new IllegalStateException("Unsupported authentication principal type: " + principal.getClass());
    }
}