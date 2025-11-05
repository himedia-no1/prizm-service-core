package run.prizm.core.auth.security.oauth2;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import run.prizm.core.auth.domain.User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User implements org.springframework.security.oauth2.core.user.OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

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
        return user.getUuid().toString();
    }
}