package run.prizm.core.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.auth.dto.TokenRefreshResponse;
import run.prizm.core.security.cookie.CookieService;
import run.prizm.core.security.jwt.JwtService;
import run.prizm.core.storage.redis.RefreshTokenCacheRepository;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.repository.UserRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenCacheRepository refreshTokenCacheRepository;
    private final UserRepository userRepository;

    @Transactional
    public TokenRefreshResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.extractRefreshTokenFromCookies(request);
        if (refreshToken == null) {
            throw new RuntimeException("Refresh token not found");
        }

        // 1. JWT 검증 (Secret + 만료시간)
        if (!jwtService.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid or expired refresh token");
        }

        // 2. Redis 비교
        RefreshTokenCacheRepository.RefreshTokenData tokenData = refreshTokenCacheRepository.findByToken(refreshToken);
        if (tokenData == null || !tokenData.token().equals(refreshToken)) {
            throw new RuntimeException("Refresh token not found in storage");
        }

        User user = userRepository.findById(tokenData.id())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDeletedAt() != null) {
            throw new RuntimeException("User is deleted");
        }

        // Rotation 제거: 새로운 Access Token만 발급
        String newAccessToken = jwtService.generateAccessToken(user);

        return new TokenRefreshResponse(newAccessToken);
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.extractRefreshTokenFromCookies(request);
        if (refreshToken != null) {
            refreshTokenCacheRepository.delete(refreshToken);
        }
        cookieService.deleteRefreshToken(response);
    }

    @Transactional
    public void withdraw(Long userId, HttpServletRequest request, HttpServletResponse response) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setDeletedAt(Instant.now());
        userRepository.save(user);

        String refreshToken = cookieService.extractRefreshTokenFromCookies(request);
        if (refreshToken != null) {
            refreshTokenCacheRepository.delete(refreshToken);
        }
        cookieService.deleteRefreshToken(response);
    }
}
