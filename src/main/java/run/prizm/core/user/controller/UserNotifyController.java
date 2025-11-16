package run.prizm.core.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.prizm.core.user.dto.UserNotifyListResponse;
import run.prizm.core.user.resolver.CurrentUser;
import run.prizm.core.user.service.UserNotifyService;

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
}
