package run.prizm.auth.user.dto;

public record OAuth2UserData(
    String providerId,
    String email,
    String name,
    String profileImage
) {}