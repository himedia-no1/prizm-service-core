package run.prizm.core.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.auth.entity.User;
import run.prizm.core.auth.repository.UserRepository;
import run.prizm.core.security.jwt.JwtService;
import run.prizm.core.security.jwt.Token;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Token refreshAccessToken(String refreshToken) {
        if (!jwtService.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid token");
        }

        String type = jwtService.getTypeFromToken(refreshToken);
        String subject = jwtService.getSubjectFromToken(refreshToken);

        if (!"user".equals(type) || subject == null) {
            throw new RuntimeException("Invalid token");
        }

        try {
            UUID userUuid = UUID.fromString(subject);
            User user = userRepository.findByUuid(userUuid)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            String accessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            long expiresIn = jwtService.getAccessTokenExpirationInSeconds();
            return new Token(accessToken, newRefreshToken, expiresIn);
        } catch (IllegalArgumentException exception) {
            throw new RuntimeException("Invalid token", exception);
        }
    }
}