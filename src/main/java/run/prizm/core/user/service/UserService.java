package run.prizm.core.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.prizm.core.common.constant.FileDirectory;
import run.prizm.core.common.exception.BusinessException;
import run.prizm.core.common.exception.ErrorCode;
import run.prizm.core.common.util.ImageUploadHelper;
import run.prizm.core.file.entity.File;
import run.prizm.core.user.dto.*;
import run.prizm.core.user.entity.User;
import run.prizm.core.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ImageUploadHelper imageUploadHelper;

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                                  .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

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
                                  .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (request.profileImage() != null && !request.profileImage()
                                                      .isEmpty()) {
            if (user.getImage() != null) {
                imageUploadHelper.deleteImage(user.getImage());
            }

            File newImage = imageUploadHelper.uploadImage(request.profileImage(), FileDirectory.USER_PROFILES.getPath());
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
                                  .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        user.setLanguage(request.language());
        userRepository.save(user);

        return getProfile(userId);
    }
}