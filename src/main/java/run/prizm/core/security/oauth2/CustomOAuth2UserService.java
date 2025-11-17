package run.prizm.core.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.common.util.ImageUploadHelper;
import run.prizm.core.file.entity.File;
import run.prizm.core.security.oauth2.extractor.OAuth2AttributeExtractor;
import run.prizm.core.user.constraint.UserAuthProvider;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.repository.UserRepository;

import java.util.Collection;
import java.util.Collections;
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
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2AttributeExtractor extractor = findExtractor(registrationId);
        OAuth2UserData userData = extractor.extract(oAuth2User.getAttributes());
        
        String languageParam = getLanguageFromRequest();
        
        User user = authenticateWithOAuth2(registrationId, userData, languageParam);
        return createOAuth2User(user, oAuth2User.getAttributes());
    }

    private OAuth2AttributeExtractor findExtractor(String provider) {
        return extractors.stream()
                         .filter(extractor -> extractor.supports(provider))
                         .findFirst()
                         .orElseThrow(() -> new RuntimeException("Unsupported OAuth provider"));
    }

    private User authenticateWithOAuth2(String registrationId, OAuth2UserData userData, String languageParam) {
        UserAuthProvider userAuthProvider = UserAuthProvider.valueOf(registrationId.toUpperCase());
        return userRepository.findByAuthProviderAndOpenidSub(userAuthProvider, userData.providerId())
                             .map(user -> {
                                 user.setName(userData.name());
                                 user.setEmail(userData.email());
                                 
                                 if (languageParam != null) {
                                     try {
                                         Language newLanguage = Language.valueOf(languageParam.toUpperCase());
                                         if (user.getLanguage() != newLanguage) {
                                             user.setLanguage(newLanguage);
                                         }
                                     } catch (IllegalArgumentException ignored) {
                                     }
                                 }
                                 
                                 return userRepository.save(user);
                             })
                             .orElseGet(() -> {
                                 Language language = Language.EN;
                                 if (languageParam != null) {
                                     try {
                                         language = Language.valueOf(languageParam.toUpperCase());
                                     } catch (IllegalArgumentException ignored) {
                                     }
                                 }
                                 
                                 File profileImage = null;
                                 if (userData.profileImage() != null && !userData.profileImage().isEmpty()) {
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
                                                 .language(language)
                                                 .active(true)
                                                 .build();
                                 return userRepository.save(user);
                             });
    }

    private String getLanguageFromRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getParameter("lang");
        }
        return null;
    }

    private OAuth2User createOAuth2User(User user, Map<String, Object> attributes) {
        return new CustomOAuth2User(user, attributes);
    }
}
