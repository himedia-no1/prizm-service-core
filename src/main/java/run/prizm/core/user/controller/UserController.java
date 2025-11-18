package run.prizm.core.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import run.prizm.core.user.dto.*;
import run.prizm.core.user.resolver.CurrentUser;
import run.prizm.core.user.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getProfile(@CurrentUser Long userId) {
        UserProfileResponse response = userService.getProfile(userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @CurrentUser Long userId,
            @Valid @ModelAttribute UserProfileUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/language")
    public ResponseEntity<UserProfileResponse> updateLanguage(
            @CurrentUser Long userId,
            @Valid @RequestBody UserLanguageUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateLanguage(userId, request);
        return ResponseEntity.ok(response);
    }
}