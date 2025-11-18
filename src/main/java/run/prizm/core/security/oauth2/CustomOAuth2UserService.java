package run.prizm.core.security.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.common.util.ImageUploadHelper;
import run.prizm.core.file.entity.File;
import run.prizm.core.security.oauth2.extractor.OAuth2AttributeExtractor;
import run.prizm.core.user.constraint.UserAuthProvider;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.repository.UserRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final List<OAuth2AttributeExtractor> extractors;
    private final UserRepository userRepository;
    private final ImageUploadHelper imageUploadHelper;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration()
                                           .getRegistrationId();
        OAuth2AttributeExtractor extractor = findExtractor(registrationId);
        OAuth2UserData userData = extractor.extract(oAuth2User.getAttributes());
        validateUserData(userData, registrationId);

        AuthenticationResult authenticationResult = authenticateWithOAuth2(registrationId, userData);
        return createOAuth2User(authenticationResult.user(), oAuth2User.getAttributes(), authenticationResult.isNewUser());
    }

    private OAuth2AttributeExtractor findExtractor(String provider) {
        return extractors.stream()
                         .filter(extractor -> extractor.supports(provider))
                         .findFirst()
                         .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT_VALUE));
    }

    private AuthenticationResult authenticateWithOAuth2(String registrationId, OAuth2UserData userData) {
        UserAuthProvider userAuthProvider = UserAuthProvider.valueOf(registrationId.toUpperCase());
        return userRepository.findByAuthProviderAndOpenidSub(userAuthProvider, userData.providerId())
                             .map(user -> {
                                 user.setName(userData.name());
                                 user.setEmail(userData.email());
                                 return new AuthenticationResult(userRepository.save(user), false);
                             })
                             .orElseGet(() -> {
                                 File profileImage = null;
                                 if (userData.profileImage() != null && !userData.profileImage()
                                                                                 .isEmpty()) {
                                     profileImage = imageUploadHelper.uploadImageFromUrl(
                                             userData.profileImage(),
                                             "profiles"
                                     );
                                 }

                                 User user = User.builder()
                                                 .authProvider(userAuthProvider)
                                                 .openidSub(userData.providerId())
                                                 .image(profileImage)
                                                 .name(userData.name())
                                                 .email(userData.email())
                                                 .language(Language.EN)
                                                 .active(true)
                                                 .build();
                                 return new AuthenticationResult(userRepository.save(user), true);
                             });
    }

    private OAuth2User createOAuth2User(User user, Map<String, Object> attributes, boolean newUser) {
        return new CustomOAuth2User(user, attributes, newUser);
    }

    private void validateUserData(OAuth2UserData userData, String provider) {
        if (!StringUtils.hasText(userData.email()) || !StringUtils.hasText(userData.name())) {
            throw new BusinessException(ErrorCode.INVALID_AUTHENTICATION, "Missing required profile information from " + provider);
        }
    }

    private record AuthenticationResult(User user, boolean isNewUser) {
    }
}
