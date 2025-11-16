package run.prizm.core.security.oauth2;

public record OAuth2UserData(
        String providerId,
        String email,
        String name,
        String profileImage
) {
}