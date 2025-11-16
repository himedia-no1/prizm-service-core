package run.prizm.core.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.user.repository.UserRepository;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.user.entity.User;
import run.prizm.core.file.entity.File;
import run.prizm.core.file.repository.FileRepository;
import run.prizm.core.security.jwt.JwtService;
import run.prizm.core.security.jwt.Token;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final FileRepository fileRepository;

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
    public User updateUserProfile(Long userId, String name, String email, Long imageId, Language language) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(name);
        user.setEmail(email);
        if (imageId != null) {
            File image = fileRepository.findById(imageId)
                                       .orElseThrow(() -> new RuntimeException("Image not found"));
            user.setImage(image);
        } else {
            user.setImage(null);
        }
        user.setLanguage(language);

        return userRepository.save(user);
    }

    @Transactional
    public void softDeleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setDeletedAt(Instant.now());

        userRepository.save(user);
    }
}