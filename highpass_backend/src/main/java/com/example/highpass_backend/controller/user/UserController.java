package com.example.highpass_backend.controller.user;

import com.example.highpass_backend.config.CookieUtils;
import com.example.highpass_backend.dto.user.UpdateAvatarRequest;
import com.example.highpass_backend.dto.user.UpdatePasswordRequest;
import com.example.highpass_backend.dto.user.UpdateUserRequest;
import com.example.highpass_backend.dto.user.UserResponse;
import com.example.highpass_backend.dto.user.VerifyPasswordRequest;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.service.auth.RefreshTokenService;
import com.example.highpass_backend.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtils cookieUtils;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal CustomJwtPrincipal principal
    ) {
        return ResponseEntity.ok(userService.getUserById(principal.getUserId()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<UserResponse> updateUser(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody UpdateUserRequest request
    ) {
        return ResponseEntity.ok(userService.updateUser(principal.getUserId(), request));
    }

    @PatchMapping("/me/avatar")
    public ResponseEntity<UserResponse> updateAvatar(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody UpdateAvatarRequest request
    ) {
        return ResponseEntity.ok(userService.updateAvatar(principal.getUserId(), request));
    }

    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody UpdatePasswordRequest request
    ) {
        userService.updatePassword(principal.getUserId(), request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/me/password/verify")
    public ResponseEntity<Void> verifyPassword(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            @Valid @RequestBody VerifyPasswordRequest request
    ) {
        userService.verifyPassword(principal.getUserId(), request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawUser(
            @AuthenticationPrincipal CustomJwtPrincipal principal,
            HttpServletResponse response
    ) {
        userService.withdrawUser(principal.getUserId(), principal.getUserId());
        refreshTokenService.delete(principal.getUserId());
        cookieUtils.deleteAccessTokenCookie(response);
        cookieUtils.deleteRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }
}
