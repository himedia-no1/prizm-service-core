package run.prizm.core.auth.dto;

public record OAuth2UserData(
    String providerId,
    String email,
    String name,
    String profileImage
) {}