package com.example.highpass_backend.dto.admin;

import com.example.highpass_backend.entity.user.User;

import java.time.LocalDateTime;
import java.util.Locale;

public record AdminUserResponse(
        String id,
        String email,
        String nickname,
        String role,
        String status,
        String avatarVisualClassName,
        LocalDateTime createdAt,
        LocalDateTime lastSeenAt,
        LocalDateTime deletedAt,
        String region,
        String loginType,
        String socialProvider,
        boolean online,
        int posts,
        int comments,
        int reports,
        String gender,
        String ageRange
) {
    public static AdminUserResponse from(
            User user,
            String socialProvider,
            boolean online,
            int posts,
            int comments,
            int reports
    ) {
        User.Status status = user.getStatus() == null ? User.Status.ACTIVE : user.getStatus();
        String provider = safe(socialProvider);
        String region = buildRegion(user);
        String nickname = safe(user.getNickname());

        return new AdminUserResponse(
                String.valueOf(user.getId()),
                safe(user.getEmail()),
                nickname,
                user.getRole().name(),
                status.name().toLowerCase(Locale.ROOT),
                user.getAvatarVisualClassName(),
                user.getCreatedAt(),
                user.getLastSeenAt(),
                user.getDeletedAt(),
                region,
                provider.isBlank() ? "local" : "social",
                provider,
                online,
                posts,
                comments,
                reports,
                safe(user.getGender()),
                safe(user.getAgeRange())
        );
    }

    private static String buildRegion(User user) {
        String siDo = safe(user.getSiDo());
        String gunGu = safe(user.getGunGu());
        return (siDo + " " + gunGu).trim();
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
