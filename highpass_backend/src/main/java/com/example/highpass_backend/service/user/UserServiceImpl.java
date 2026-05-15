package com.example.highpass_backend.service.user;

import com.example.highpass_backend.dto.user.UpdatePasswordRequest;
import com.example.highpass_backend.dto.user.UpdateAvatarRequest;
import com.example.highpass_backend.dto.user.UpdateUserRequest;
import com.example.highpass_backend.dto.user.UserResponse;
import com.example.highpass_backend.dto.user.VerifyPasswordRequest;
import com.example.highpass_backend.eception.BusinessException;
import com.example.highpass_backend.eception.ErrorCode;
import com.example.highpass_backend.entity.auth.OAuth2Account;
import com.example.highpass_backend.entity.user.User;
import com.example.highpass_backend.repository.auth.OAuth2AccountRepository;
import com.example.highpass_backend.repository.user.UserRepository;
import com.example.highpass_backend.util.NicknameNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private static final Set<String> ALLOWED_AVATAR_VISUAL_CLASSES = Set.of(
            "bg-hp-100 font-bold text-hp-700",
            "bg-slate-900 font-bold text-white",
            "bg-sky-100 font-bold text-sky-700",
            "bg-emerald-100 font-bold text-emerald-700",
            "bg-rose-100 font-bold text-rose-700",
            "bg-amber-100 font-bold text-amber-700"
    );

    private final UserRepository userRepository;
    private final OAuth2AccountRepository oauth2AccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserPresenceService userPresenceService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다."));

        return toUserResponse(user);
    }

    @Override
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다."));
        String sanitizedNickname = NicknameNormalizer.sanitizeForStorage(request.getNickname());

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            validateCurrentPassword(user, request.getCurrentPassword());
        }

        validateNicknameAvailability(sanitizedNickname, user.getId());

        user.updateProfile(
                sanitizedNickname,
                request.getAgeRange(),
                request.getGender(),
                request.getSiDo(),
                request.getGunGu()
        );

        return toUserResponse(user);
    }

    @Override
    public UserResponse updateAvatar(Long userId, UpdateAvatarRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다."));

        String avatarVisualClassName = request.getAvatarVisualClassName();
        String normalizedAvatarVisualClassName =
                avatarVisualClassName == null || avatarVisualClassName.isBlank()
                        ? null
                        : avatarVisualClassName.trim();

        if (
                normalizedAvatarVisualClassName != null
                        && !ALLOWED_AVATAR_VISUAL_CLASSES.contains(normalizedAvatarVisualClassName)
                        && !normalizedAvatarVisualClassName.matches("^#[0-9A-Fa-f]{6}$")
                        && !normalizedAvatarVisualClassName.matches("^#[0-9A-Fa-f]{6}\\|#[0-9A-Fa-f]{6}$")
        ) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "지원하지 않는 아바타 스타일입니다.");
        }

        user.updateAvatarVisualClassName(normalizedAvatarVisualClassName);

        return toUserResponse(user);
    }

    @Override
    public void updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다."));

        validateCurrentPassword(user, request.getCurrentPassword());
        user.encodePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    @Override
    public void verifyPassword(Long userId, VerifyPasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다."));

        validateCurrentPassword(user, request.getCurrentPassword());
    }

    @Override
    public void withdrawUser(Long authenticatedUserId, Long userId) {
        if (!authenticatedUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "본인 계정만 탈퇴 처리할 수 있습니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "존재하지 않는 회원입니다."));

        if (user.getStatus() == User.Status.DELETED) {
            return;
        }

        user.updateStatus(User.Status.DELETED);
        user.markSeen();
    }

    private void validateCurrentPassword(User user, String currentPassword) {
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION, "소셜 로그인 계정은 비밀번호 확인으로 수정할 수 없습니다.");
        }

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "현재 비밀번호를 입력해 주세요.");
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "현재 비밀번호가 일치하지 않습니다.");
        }
    }

    private void validateNicknameAvailability(String nickname, Long excludeUserId) {
        if (nickname == null || nickname.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "닉네임을 입력해 주세요.");
        }
        if (userRepository.existsByNicknameAndIdNot(nickname, excludeUserId)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 사용 중인 닉네임입니다.");
        }
    }

    private UserResponse toUserResponse(User user) {
        String socialProvider = oauth2AccountRepository.findAllByUserId(user.getId()).stream()
                .findFirst()
                .map(OAuth2Account::getProvider)
                .map(Enum::name)
                .orElse(null);

        return UserResponse.from(user, socialProvider, userPresenceService.isOnline(user.getId()));
    }
}
