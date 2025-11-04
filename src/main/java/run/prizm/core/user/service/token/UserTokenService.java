package run.prizm.core.user.service.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.common.constant.ErrorCode;
import run.prizm.core.common.constant.UserType;
import run.prizm.core.common.dto.Token;
import run.prizm.core.common.dto.TokenClaims;
import run.prizm.core.common.exception.AuthException;
import run.prizm.core.common.service.token.JwtProvider;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.repository.UserRepository;

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