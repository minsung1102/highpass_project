package com.example.highpass_backend.dto.user;

import com.example.highpass_backend.entity.user.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String nickname;
    private String ageRange;
    private String gender;
    private String siDo;
    private String gunGu;
    private String role;
    private String status;
    private String loginType;
    private String socialProvider;
    private boolean online;
    private LocalDateTime lastSeenAt;
    private String avatarVisualClassName;


    public static UserResponse from(User user) {
        return from(user, null);
    }

    public static UserResponse from(User user, String socialProvider) {
        return from(user, socialProvider, false);
    }

    public static UserResponse from(User user, String socialProvider, boolean online) {
        boolean deleted = user.getStatus() == User.Status.DELETED;

        return UserResponse.builder()
                .id(user.getId())
                .email(deleted ? null : user.getEmail())
                .nickname(UserDisplayName.nickname(user))
                .ageRange(deleted ? null : user.getAgeRange())
                .gender(deleted ? null : user.getGender())
                .siDo(deleted ? null : user.getSiDo())
                .gunGu(deleted ? null : user.getGunGu())
                .role(user.getRole().name())
                .status(user.getStatus().name().toLowerCase())
                .loginType(deleted ? null : (socialProvider == null ? "local" : "social"))
                .socialProvider(deleted ? null : socialProvider)
                .online(!deleted && online)
                .lastSeenAt(deleted ? null : user.getLastSeenAt())
                .avatarVisualClassName(deleted ? null : user.getAvatarVisualClassName())
                .build();
    }
}
