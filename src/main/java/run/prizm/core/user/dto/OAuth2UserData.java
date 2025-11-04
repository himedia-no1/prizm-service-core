package run.prizm.core.user.dto;

public record OAuth2UserData(
    String providerId,
    String email,
    String name,
    String profileImage
) {}