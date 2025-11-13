package run.prizm.core.security.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.auth.entity.User;
import run.prizm.core.auth.entity.UserAuthProvider;
import run.prizm.core.auth.repository.UserRepository;
import run.prizm.core.security.oauth2.extractor.OAuth2AttributeExtractor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final List<OAuth2AttributeExtractor> extractors;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        OAuth2AttributeExtractor extractor = findExtractor(registrationId);
        OAuth2UserData userData = extractor.extract(oAuth2User.getAttributes());
        
        User user = authenticateWithOAuth2(registrationId, userData);
        
        return createOAuth2User(user, oAuth2User.getAttributes());
    }

    private OAuth2AttributeExtractor findExtractor(String provider) {
        return extractors.stream()
                .filter(extractor -> extractor.supports(provider))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unsupported OAuth provider"));
    }

        private User authenticateWithOAuth2(String registrationId, OAuth2UserData userData) {

            UserAuthProvider authProvider = UserAuthProvider.valueOf(registrationId.toUpperCase());

    

            return userRepository.findByAuthProviderAndOpenidSub(authProvider, userData.providerId())

                    .map(user -> {

                        user.setName(userData.name());

                        user.setEmail(userData.email());

                        return userRepository.save(user);

                    })

                    .orElseGet(() -> {

                        User user = User.builder()

                                .authProvider(authProvider)

                                .openidSub(userData.providerId())

                                .profileImagePath(userData.profileImage())

                                .name(userData.name())

                                .email(userData.email())

                                .language(run.prizm.core.auth.entity.Language.EN)

                                .build();

                        return userRepository.save(user);

                    });

        }

    

        private OAuth2User createOAuth2User(User user, Map<String, Object> attributes) {

            return new OAuth2User() {

                @Override

                public Map<String, Object> getAttributes() {

                    return attributes;

                }

    

                @Override

                public Collection<? extends GrantedAuthority> getAuthorities() {

                    return Collections.emptyList();

                }

    

                @Override

                public String getName() {

                    return user.getId().toString();

                }

    

                public User getUser() {

                    return user;

                }

            };

        }
}