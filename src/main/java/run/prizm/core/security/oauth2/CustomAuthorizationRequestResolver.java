package run.prizm.core.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import run.prizm.core.properties.FrontendProperties;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;
    private final FrontendProperties frontendProperties;

    public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository,
                                              FrontendProperties frontendProperties) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, "/api/auth/oauth2");
        this.frontendProperties = frontendProperties;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return authorizationRequest != null ? customizeAuthorizationRequest(authorizationRequest, request) : null;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return authorizationRequest != null ? customizeAuthorizationRequest(authorizationRequest, request) : null;
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request) {

        Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());

        String lang = request.getParameter("lang");
        String inviteCode = request.getParameter("invite");

        if (lang != null) {
            additionalParameters.put("lang", lang);
        }
        if (inviteCode != null) {
            additionalParameters.put("invite", inviteCode);
        }

        String originalRedirectUri = authorizationRequest.getRedirectUri();
        String path = originalRedirectUri.substring(originalRedirectUri.indexOf("/api/"));
        String frontendRedirectUri = frontendProperties.getUrl() + path;

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .redirectUri(frontendRedirectUri)
                .additionalParameters(additionalParameters)
                .build();
    }
}
