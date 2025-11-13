package run.prizm.core.security.jwt;

public record Token(String accessToken, String refreshToken, long expiresIn, String tokenType) {

    public Token(String accessToken, String refreshToken, long expiresIn) {
        this(accessToken, refreshToken, expiresIn, "Bearer");
    }
}