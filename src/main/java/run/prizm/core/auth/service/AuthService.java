package run.prizm.core.auth.service;

import jakarta.servlet.http.Cookie;
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
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final CookieService cookieService;
    private final RefreshTokenCacheRepository refreshTokenCacheRepository;
    private final UserRepository userRepository;

    @Transactional
    public TokenRefreshResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request);
        if (refreshToken == null) {
            throw new RuntimeException("Refresh token not found");
        }

        Long userId = refreshTokenCacheRepository.findUserIdByToken(refreshToken);
        if (userId == null) {
            throw new RuntimeException("Invalid refresh token");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getDeletedAt() != null) {
            throw new RuntimeException("User is deleted");
        }

        refreshTokenCacheRepository.delete(refreshToken);

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken();

        refreshTokenCacheRepository.save(newRefreshToken, user.getId());
        cookieService.setRefreshToken(response, newRefreshToken);

        return new TokenRefreshResponse(newAccessToken, jwtService.getAccessTokenExpirationInSeconds());
    }

    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractRefreshTokenFromCookies(request);
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

        String refreshToken = extractRefreshTokenFromCookies(request);
        if (refreshToken != null) {
            refreshTokenCacheRepository.delete(refreshToken);
        }
        cookieService.deleteRefreshToken(response);
    }

    private String extractRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(cookie -> "refresh_token".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);
    }
}
