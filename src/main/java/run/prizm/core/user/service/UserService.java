package run.prizm.core.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.common.util.ImageUploadHelper;
import run.prizm.core.file.entity.File;
import run.prizm.core.user.repository.UserRepository;
import run.prizm.core.common.constraint.Language;
import run.prizm.core.user.dto.*;
import run.prizm.core.user.entity.User;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageUploadHelper imageUploadHelper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String LAST_PATH_KEY_PREFIX = "user:last_path:";

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String profileImage = imageUploadHelper.getImageUrl(user.getImage());

        return new UserProfileResponse(
                profileImage,
                user.getName(),
                user.getEmail(),
                user.getAuthProvider(),
                user.getLanguage(),
                user.getCreatedAt()
        );
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UserProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.profileImage() != null && !request.profileImage().isEmpty()) {
            // 기존 이미지 삭제
            if (user.getImage() != null) {
                imageUploadHelper.deleteImage(user.getImage());
            }
            
            // 새 이미지 업로드 및 저장
            File newImage = imageUploadHelper.uploadImage(request.profileImage(), "profiles");
            user.setImage(newImage);
        }

        if (request.name() != null) {
            user.setName(request.name());
        }

        userRepository.save(user);
        return getProfile(userId);
    }

    @Transactional
    public UserProfileResponse updateLanguage(Long userId, UserLanguageUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setLanguage(request.language());
        userRepository.save(user);

        return getProfile(userId);
    }

    public void saveLastPath(Long userId, UserLastPathRequest request) {
        String key = LAST_PATH_KEY_PREFIX + userId;
        redisTemplate.opsForValue().set(key, request.getPath());
    }

    public UserLastPathResponse getLastPath(Long userId) {
        String key = LAST_PATH_KEY_PREFIX + userId;
        String path = (String) redisTemplate.opsForValue().get(key);
        return UserLastPathResponse.builder()
                .path(path)
                .build();
    }
}