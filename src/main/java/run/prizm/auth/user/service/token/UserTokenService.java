package run.prizm.auth.user.service.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.auth.common.constant.ErrorCode;
import run.prizm.auth.common.constant.UserType;
import run.prizm.auth.common.dto.Token;
import run.prizm.auth.common.dto.TokenClaims;
import run.prizm.auth.common.exception.AuthException;
import run.prizm.auth.common.service.token.JwtProvider;
import run.prizm.auth.user.entity.User;
import run.prizm.auth.user.repository.UserRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserTokenService {

    private final JwtProvider jwtProvider;
    private final UserTokenClaimsMapper claimsMapper;
    private final UserRepository userRepository;

    public Token generateToken(User user) {
        TokenClaims accessClaims = claimsMapper.toAccessClaims(user);
        TokenClaims refreshClaims = claimsMapper.toRefreshClaims(user);
        return createToken(accessClaims, refreshClaims);
    }

    @Transactional(readOnly = true)
    public Token refreshAccessToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        }

        String type = jwtProvider.getTypeFromToken(refreshToken);
        String subject = jwtProvider.getSubjectFromToken(refreshToken);

        if (!UserType.USER.getValue().equals(type) || subject == null) {
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        }

        try {
            UUID userUuid = UUID.fromString(subject);
            User user = userRepository.getActiveByUuidOrThrow(userUuid);
            return generateToken(user);
        } catch (IllegalArgumentException exception) {
            throw new AuthException(ErrorCode.INVALID_TOKEN, exception);
        }
    }

    private Token createToken(TokenClaims accessClaims, TokenClaims refreshClaims) {
        String accessToken = jwtProvider.generateAccessToken(accessClaims);
        String refreshToken = jwtProvider.generateRefreshToken(refreshClaims);
        long expiresIn = jwtProvider.getAccessTokenExpirationInSeconds();
        return new Token(accessToken, refreshToken, expiresIn);
    }
}
