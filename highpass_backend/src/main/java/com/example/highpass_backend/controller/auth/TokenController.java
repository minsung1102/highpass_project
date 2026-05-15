package com.example.highpass_backend.controller.auth;

import com.example.highpass_backend.config.CookieUtils;
import com.example.highpass_backend.dto.etc.ApiResponse;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.auth.RefreshToken;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.user.UserRepository;
import com.example.highpass_backend.security.CustomJwtPrincipal;
import com.example.highpass_backend.security.JwtProperties;
import com.example.highpass_backend.security.JwtTokenProvider;
import com.example.highpass_backend.service.auth.RefreshTokenService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class TokenController {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final CookieUtils cookieUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtils.getCookieValue(request, "refresh_token");

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            failRefresh(response, "리프레시 토큰이 유효하지 않습니다.");
        }

        if (!"refresh".equals(jwtTokenProvider.getTokenType(refreshToken))) {
            failRefresh(response, "잘못된 리프레시 토큰입니다.");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        RefreshToken saved = refreshTokenService.getByUserId(userId);

        if (!saved.getToken().equals(refreshToken)) {
            failRefresh(response, "저장된 리프레시 토큰과 일치하지 않습니다.");
        }

        if (saved.getExpiryAt().isBefore(LocalDateTime.now())) {
            refreshTokenService.delete(userId);
            failRefresh(response, "만료된 리프레시 토큰입니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다."));
        String accessToken = jwtTokenProvider.createAccessToken(userId, user.getEmail(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId);

        refreshTokenService.save(
                userId,
                newRefreshToken,
                LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000)
        );

        cookieUtils.addAccessTokenCookie(response, accessToken);
        cookieUtils.addRefreshTokenCookie(response, newRefreshToken);

        return ResponseEntity.ok(new ApiResponse("토큰 재발급 완료"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieUtils.getCookieValue(request, "refresh_token");
        Long userIdToDelete = null;

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomJwtPrincipal principal) {
            userIdToDelete = principal.getUserId();
        } else if (refreshToken != null) {
            try {
                Long userId = jwtTokenProvider.getUserIdAllowExpired(refreshToken);
                RefreshToken saved = refreshTokenService.findByUserId(userId);

                if (saved != null && saved.getToken().equals(refreshToken)) {
                    userIdToDelete = userId;
                }
            } catch (JwtException | IllegalArgumentException ignored) {
                // Invalid refresh token should not block cookie cleanup during logout.
            }
        }

        if (userIdToDelete != null) {
            refreshTokenService.delete(userIdToDelete);
        }

        cookieUtils.deleteAccessTokenCookie(response);
        cookieUtils.deleteRefreshTokenCookie(response);

        return ResponseEntity.ok(new ApiResponse("로그아웃 완료"));
    }

    private void failRefresh(HttpServletResponse response, String message) {
        cookieUtils.deleteAccessTokenCookie(response);
        cookieUtils.deleteRefreshTokenCookie(response);
        throw new BusinessException(ErrorCode.UNAUTHORIZED, message);
    }
}
