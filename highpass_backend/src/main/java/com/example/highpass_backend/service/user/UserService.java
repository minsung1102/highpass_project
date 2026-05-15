package com.example.highpass_backend.service.user;

import com.example.highpass_backend.dto.user.UpdateUserRequest;
import com.example.highpass_backend.dto.user.UpdateAvatarRequest;
import com.example.highpass_backend.dto.user.UpdatePasswordRequest;
import com.example.highpass_backend.dto.user.UserResponse;
import com.example.highpass_backend.dto.user.VerifyPasswordRequest;

public interface UserService {
    UserResponse getUserById(Long userId);
    UserResponse updateUser(Long userId, UpdateUserRequest request);
    UserResponse updateAvatar(Long userId, UpdateAvatarRequest request);
    void updatePassword(Long userId, UpdatePasswordRequest request);
    void verifyPassword(Long userId, VerifyPasswordRequest request);
    void withdrawUser(Long authenticatedUserId, Long userId);
}
