package run.prizm.core.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import run.prizm.core.user.dto.UserNotifyListResponse;
import run.prizm.core.user.resolver.CurrentUser;
import run.prizm.core.user.service.UserNotifyService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class UserNotifyController {

    private final UserNotifyService userNotifyService;

    @GetMapping
    public ResponseEntity<UserNotifyListResponse> getNotifications(
            @CurrentUser Long userId
    ) {
        UserNotifyListResponse response = userNotifyService.getNotifications(userId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping
    public ResponseEntity<Void> markAsRead(
            @CurrentUser Long userId,
            @RequestBody List<Long> notificationIds
    ) {
        userNotifyService.markAsRead(userId, notificationIds);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteNotifications(
            @CurrentUser Long userId,
            @RequestBody List<Long> notificationIds
    ) {
        userNotifyService.deleteNotifications(userId, notificationIds);
        return ResponseEntity.noContent().build();
    }
}
