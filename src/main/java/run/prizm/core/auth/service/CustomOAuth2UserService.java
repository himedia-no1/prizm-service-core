package run.prizm.core.auth.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.auth.constant.ErrorCode;
import run.prizm.core.auth.exception.AuthException;
import run.prizm.core.auth.dto.OAuth2UserData;
import run.prizm.core.auth.domain.User;
import run.prizm.core.auth.domain.UserAuthProvider;
import run.prizm.core.auth.repository.UserRepository;
import run.prizm.core.auth.security.oauth2.CustomOAuth2User;
import run.prizm.core.auth.service.oauth2.extractor.OAuth2AttributeExtractor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final List<OAuth2AttributeExtractor> extractors;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CustomOAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        return processOAuth2User(oAuth2User, registrationId);
    }

    public OAuth2AttributeExtractor findExtractor(String provider) {
        return extractors.stream()
                .filter(extractor -> extractor.supports(provider))
                .findFirst()
                .orElseThrow(() -> new AuthException(ErrorCode.UNSUPPORTED_OAUTH_PROVIDER));
    }

    public CustomOAuth2User processOAuth2User(OAuth2User oAuth2User, String registrationId) {
        OAuth2AttributeExtractor extractor = findExtractor(registrationId);
        OAuth2UserData userData = extractor.extract(oAuth2User.getAttributes());
        return loadOrCreateUser(registrationId, oAuth2User.getAttributes(), userData);
    }

    private User updateExistingUser(User user, OAuth2UserData userData) {
        user.updateProfile(userData.profileImage(), userData.name(), userData.email());
        return userRepository.save(user);
    }

    private User createNewUser(UserAuthProvider authProvider, OAuth2UserData userData) {
        User user = User.builder()
                        .authProvider(authProvider)
                        .openidSub(userData.providerId())
                        .profileImage(userData.profileImage())
                        .globalName(userData.name())
                        .globalEmail(userData.email())
                        .build();
        return userRepository.save(user);
    }

    private CustomOAuth2User loadOrCreateUser(String registrationId, Map<String, Object> attributes, OAuth2UserData userData) {
        UserAuthProvider authProvider = UserAuthProvider.valueOf(registrationId.toUpperCase());

        User user = userRepository.findByAuthProviderAndOpenidSub(authProvider, userData.providerId())
                                  .map(existingUser -> updateExistingUser(existingUser, userData))
                                  .orElseGet(() -> createNewUser(authProvider, userData));

        return new CustomOAuth2User(user, attributes);
    }
}