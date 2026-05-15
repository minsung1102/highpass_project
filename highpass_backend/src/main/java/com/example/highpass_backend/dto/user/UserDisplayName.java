package com.example.highpass_backend.dto.user;

import com.example.highpass_backend.entity.user.User;

public final class UserDisplayName {

    private static final String DELETED_USER_NICKNAME = "탈퇴한 계정";

    private UserDisplayName() {
    }

    public static String nickname(User user) {
        if (user == null) {
            return DELETED_USER_NICKNAME;
        }
        return user.getStatus() == User.Status.DELETED ? DELETED_USER_NICKNAME : user.getNickname();
    }
}
