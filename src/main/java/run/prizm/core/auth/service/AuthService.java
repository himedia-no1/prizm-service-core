package run.prizm.core.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.auth.entity.Language;
import run.prizm.core.auth.entity.User;
import run.prizm.core.auth.repository.UserRepository;
import run.prizm.core.security.jwt.JwtService;
import run.prizm.core.security.jwt.Token;

import java.time.LocalDateTime;

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
            Long userId = Long.parseLong(subject);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String accessToken = jwtService.generateAccessToken(user);
            String newRefreshToken = jwtService.generateRefreshToken(user);
            long expiresIn = jwtService.getAccessTokenExpirationInSeconds();
            return new Token(accessToken, newRefreshToken, expiresIn);
        } catch (NumberFormatException exception) {
            throw new RuntimeException("Invalid token", exception);
        }
    }

    @Transactional
    public User updateUserProfile(Long userId, String name, String email, String profileImagePath, Language language) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(name);
        user.setEmail(email);
        user.setProfileImagePath(profileImagePath);
        user.setLanguage(language);

        return userRepository.save(user);
    }

    @Transactional
    public void softDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);
    }
}
