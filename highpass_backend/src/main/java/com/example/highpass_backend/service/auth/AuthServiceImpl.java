package com.example.highpass_backend.service.auth;

import com.example.highpass_backend.config.CookieUtils;
import com.example.highpass_backend.dto.auth.LoginResponse;
import com.example.highpass_backend.dto.auth.UserLoginRequest;
import com.example.highpass_backend.dto.auth.UserSignupRequest;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.user.UserRepository;
import com.example.highpass_backend.security.JwtProperties;
import com.example.highpass_backend.security.JwtTokenProvider;
import com.example.highpass_backend.service.user.UserPresenceService;
import com.example.highpass_backend.util.NicknameNormalizer;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final CookieUtils cookieUtils;
    private final RefreshTokenService refreshTokenService;
    private final UserPresenceService userPresenceService;

    @Override
    public LoginResponse signup(UserSignupRequest request, HttpServletResponse response) {
        String sanitizedNickname = NicknameNormalizer.sanitizeForStorage(request.getNickname());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 이메일입니다.");
        }
        if (sanitizedNickname == null || sanitizedNickname.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "닉네임을 입력해 주세요.");
        }
        if (userRepository.existsByNickname(sanitizedNickname)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 닉네임입니다.");
        }

        User user = userRepository.save(
                User.builder()
                        .email(request.getEmail())
                        .password(passwordEncoder.encode(request.getPassword()))
                        .nickname(sanitizedNickname)
                        .ageRange(request.getAgeRange())
                        .gender(request.getGender())
                        .siDo(request.getSiDo())
                        .gunGu(request.getGunGu())
                        .build()
        );

        issueAuthCookies(user, response);
        userPresenceService.markSeen(user.getId());
        return toLoginResponse(user);
    }

    @Override
    public LoginResponse login(UserLoginRequest request, HttpServletResponse response) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."));

        if (user.getPassword() == null) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "소셜 로그인 계정입니다. 구글 또는 카카오 로그인을 이용해 주세요.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다.");
        }

        if (user.getStatus() == User.Status.SUSPENDED) {
            throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
        }

        if (user.getStatus() == User.Status.DELETED) {
            throw new BusinessException(ErrorCode.ACCOUNT_DELETED);
        }

        issueAuthCookies(user, response);
        userPresenceService.markSeen(user.getId());
        return toLoginResponse(user);
    }

    private void issueAuthCookies(User user, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenService.save(
                user.getId(),
                refreshToken,
                LocalDateTime.now().plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000)
        );

        cookieUtils.addAccessTokenCookie(response, accessToken);
        cookieUtils.addRefreshTokenCookie(response, refreshToken);
    }

    private LoginResponse toLoginResponse(User user) {
        return LoginResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .ageRange(user.getAgeRange())
                .gender(user.getGender())
                .siDo(user.getSiDo())
                .gunGu(user.getGunGu())
                .role(user.getRole().name())
                .redirectUrl(user.getRole() == User.Role.ADMIN ? "/admin" : "/calendar")
                .build();
    }
}
