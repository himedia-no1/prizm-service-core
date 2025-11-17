package run.prizm.core.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import run.prizm.core.auth.dto.TokenRefreshResponse;
import run.prizm.core.auth.service.AuthService;
import run.prizm.core.user.resolver.CurrentUser;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        TokenRefreshResponse tokenResponse = authService.refresh(request, response);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.logout(request, response);
        return ResponseEntity.noContent()
                             .build();
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @CurrentUser Long userId,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        authService.withdraw(userId, request, response);
        return ResponseEntity.noContent()
                             .build();
    }
}