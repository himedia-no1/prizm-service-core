package run.prizm.core.user.controller;

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
            @ModelAttribute UserProfileUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateProfile(userId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/language")
    public ResponseEntity<UserProfileResponse> updateLanguage(
            @CurrentUser Long userId,
            @RequestBody UserLanguageUpdateRequest request
    ) {
        UserProfileResponse response = userService.updateLanguage(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/last-path")
    public ResponseEntity<Void> saveLastPath(
            @CurrentUser Long userId,
            @RequestBody UserLastPathRequest request
    ) {
        userService.saveLastPath(userId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/last-path")
    public ResponseEntity<UserLastPathResponse> getLastPath(@CurrentUser Long userId) {
        UserLastPathResponse response = userService.getLastPath(userId);
        return ResponseEntity.ok(response);
    }
}