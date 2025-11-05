package run.prizm.core.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.auth.domain.Admin;
import run.prizm.core.auth.repository.AdminRepository;
import run.prizm.core.auth.constant.ErrorCode;
import run.prizm.core.auth.constant.UserType;
import run.prizm.core.auth.dto.Token;
import run.prizm.core.auth.dto.TokenClaims;
import run.prizm.core.auth.exception.AuthException;
import run.prizm.core.auth.security.jwt.AdminTokenClaimsMapper;
import run.prizm.core.auth.security.jwt.JwtProvider;

@Service
@RequiredArgsConstructor
public class AdminTokenService {

    private final JwtProvider jwtProvider;
    private final AdminTokenClaimsMapper claimsMapper;
    private final AdminRepository adminRepository;

    public Token generateToken(Admin admin) {
        TokenClaims accessClaims = claimsMapper.toAccessClaims(admin);
        TokenClaims refreshClaims = claimsMapper.toRefreshClaims(admin);
        return createToken(accessClaims, refreshClaims);
    }

    @Transactional(readOnly = true)
    public Token refreshAccessToken(String refreshToken) {
        if (!jwtProvider.validateToken(refreshToken)) {
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        }

        String type = jwtProvider.getTypeFromToken(refreshToken);
        String subject = jwtProvider.getSubjectFromToken(refreshToken);

        if (!UserType.ADMIN.getValue().equals(type) || subject == null) {
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        }

        try {
            Long adminId = Long.parseLong(subject);
            Admin admin = adminRepository.getActiveByIdOrThrow(adminId);
            return generateToken(admin);
        } catch (NumberFormatException exception) {
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