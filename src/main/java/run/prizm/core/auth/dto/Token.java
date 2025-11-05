package run.prizm.core.auth.dto;

public record Token(String accessToken, String refreshToken, long expiresIn, String tokenType) {

    private static final String DEFAULT_TOKEN_TYPE = "Bearer";

    public Token(String accessToken, String refreshToken, long expiresIn) {
        this(accessToken, refreshToken, expiresIn, DEFAULT_TOKEN_TYPE);
    }
}